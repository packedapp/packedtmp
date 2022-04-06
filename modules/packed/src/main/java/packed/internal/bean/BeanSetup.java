package packed.internal.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSupport;
import app.packed.bean.operation.OperationMirror;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import packed.internal.bean.PackedBeanDriver.SourceType;
import packed.internal.bean.hooks.usesite.HookModel;
import packed.internal.bean.inject.DependencyNode;
import packed.internal.bean.inject.DependencyProducer;
import packed.internal.bean.inject.InternalDependency;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.RealmSetup;
import packed.internal.inject.InternalFactory;
import packed.internal.inject.manager.InjectionManager;
import packed.internal.lifetime.PoolEntryHandle;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The build-time configuration of a bean. */
public final class BeanSetup extends ComponentSetup implements DependencyProducer {

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_CONTAINER_CONFIGURATION_ON_WIRE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class,
            "onWired", void.class);

    /**
     * Non-null if a bean instance needs to be created at runtime. This include beans that have an empty constructor (no
     * actual dependencies). Null if a functional bean, or a bean instance was specified when configuring the bean.
     */
    @Nullable
    private final DependencyNode dependencyNode;

    /** The driver used to create a bean. */
    public final PackedBeanDriver<?> driver;

    /**
     * Factory that was specified if this bean was created from a Factory or Class, null if created from an instance, for
     * example, installInstance.
     * <p>
     * We only keep this around to find the default key that the bean will be exposed as if registered as a service. We lazy
     * calculate it from {@link #provideInstance(ComponentSetup)}
     */
    @Nullable
    private final InternalFactory<?> factory;

    /** A model of the hooks on the bean. */
    @Nullable
    public final HookModel hookModel;

    private InjectionManager injectionManager;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    // What if managed prototype bean????
    @Nullable
    public final PoolEntryHandle singletonHandle;

    public BeanSetup(ContainerSetup container, RealmSetup realm, PackedBeanDriver<?> driver) {
        super(container.application, realm, container);

        this.driver = driver;
        this.hookModel = driver.sourceType == SourceType.NONE ? null : realm.accessor().beanModelOf(driver.beanType);

        switch (driver.sourceType) {
        case CLASS:
            Factory<?> fac = BeanSupport.defaultFactoryFor((Class<?>) driver.source);
            this.factory = InternalFactory.canonicalize(fac);
            break;
        case FACTORY:
            this.factory = InternalFactory.canonicalize((Factory<?>) driver.source);
            break;
        case INSTANCE:
            this.factory = null;
            break;
        default:
            this.factory = null;
        }

        this.singletonHandle = driver.beanKind() == BeanKind.CONTAINER ? lifetime.pool.reserve(driver.beanType) : null;

        // Can only register a single extension bean of a particular type
        if (driver.extension != null && driver.beanKind() == BeanKind.CONTAINER) {
            driver.extension.injectionManager.addBean(driver, this);
        }

        if (driver.sourceType == SourceType.INSTANCE || driver.sourceType == SourceType.NONE) {
            // We already have a bean instance, no need to have an injection node for creating a new bean instance.
            this.dependencyNode = null;

            // Store the supplied bean instance in the lifetime (constant) pool.
            // Skal vel faktisk vaere i application poolen????
            // Ja der er helt sikker forskel paa noget der bliver initializeret naar containeren bliver initialiseret
            // og saa constant over hele applikation.
            // Skal vi overhoved have en constant pool???
            lifetime.pool.addConstant(pool -> singletonHandle.store(pool, driver.source));
            // Or maybe just bind the instance directly in the method handles.
        } else {
            List<InternalDependency> dependencies = factory.dependencies();

            // Extract a MethodHandlefrom the factory
            MethodHandle mh = realm.accessor().toMethodHandle(factory);

            this.dependencyNode = new BeanInstanceDependencyNode(this, dependencies, mh);

            container.beans.addConsumer(dependencyNode);
        }

        // Find a hook model for the bean type and wire it
        if (hookModel != null) {
            hookModel.onWire(this);

            // Set the name of the component if it have not already been set using a wirelet
            initializeNameWithPrefix(hookModel.simpleName());
        }
        // TODO naming
    }

    public Key<?> defaultKey() {
        if (factory != null) {
            return Key.convertTypeLiteral(factory.typeLiteral());
        } else {
            return hookModel.defaultKey();
        }
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // If we have a singleton accessor return a method handle that can read the single bean instance
        // Otherwise return a method handle that can instantiate a new bean
        if (singletonHandle != null) {
            return singletonHandle.poolReader(); // MethodHandle(ConstantPool)T
        } else {
            return dependencyNode.runtimeMethodHandle(); // MethodHandle(ConstantPool)T
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DependencyNode dependencyConsumer() {
        return dependencyNode;
    }

    public InjectionManager injectionManager() {
        InjectionManager m = injectionManager;
        if (m == null) {
            ExtensionSetup extension = driver.extension;
            if (extension == null) {

            } else {
                m = injectionManager = extension.injectionManager;
            }
        }

        return m;
    }

    /** {@inheritDoc} */
    @Override
    public BeanMirror mirror() {
        return new BuildTimeBeanMirror();
    }

    /** {@inheritDoc} */
    @Override
    public void onWired() {
        try {
            MH_CONTAINER_CONFIGURATION_ON_WIRE.invokeExact((ComponentConfiguration) driver.configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        super.onWired();
    }

    /** A build-time bean mirror. */
    public final class BuildTimeBeanMirror extends AbstractBuildTimeComponentMirror implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanClass() {
            return hookModel.clazz;
        }

        /** {@inheritDoc} */
        @Override
        public BeanKind beanKind() {
            return driver.beanKind();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<ComponentMirror> children() {
            return List.of();
        }

        /** {@inheritDoc} */
        public final ContainerMirror container() {
            return parent.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension<?>>> registrant() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<OperationMirror> operations() {
            return driver.operations.toMirrors();
        }
    }
}
