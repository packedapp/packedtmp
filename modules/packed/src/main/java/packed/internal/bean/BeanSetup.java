package packed.internal.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.hooks.usage.BeanType;
import app.packed.bean.mirror.BeanElementMirror;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import packed.internal.bean.hooks.usesite.HookModel;
import packed.internal.bean.inject.DependencyNode;
import packed.internal.bean.inject.DependencyProducer;
import packed.internal.bean.inject.InternalDependency;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionRealmSetup;
import packed.internal.container.RealmSetup;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolEntryHandle;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The build-time configuration of a bean. */
public final class BeanSetup extends ComponentSetup implements DependencyProducer {

    /* Information about the type of bean. */
    public final BeanType beanType;

    /**
     * Non-null if a bean instance needs to be created at runtime. This include beans that have an empty constructor (no
     * actual dependencies). Null if a functional bean, or a bean instance was specified when configuring the bean.
     */
    @Nullable
    private final DependencyNode dependencyNode;

    /**
     * Factory that was specified if this bean was created from a Factory or Class, null if created from an instance, for
     * example, installInstance.
     * <p>
     * We only keep this around to find the default key that the bean will be exposed as if registered as a service. We lazy
     * calculate it from {@link #provide(ComponentSetup)}
     */
    @Nullable
    private final Factory<?> factory;

    /** A model of the hooks on the bean. */
    public final HookModel hookModel;

    /** A service object if the source is provided as a service. */
    // Would be nice if we could move it somewhere else.. Like Listener
    @Nullable
    @Deprecated
    private ServiceSetup service;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    // What if managed prototype bean????
    @Nullable
    public final PoolEntryHandle singletonHandle;

    /** A handle for invoking the protected method {@link Extension#onClose()}. */
    private static final MethodHandle MH_CONTAINER_CONFIGURATION_ON_WIRE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class,
            "onWired", void.class);

    final PackedBeanMaker<?> beanHandle;

    @Override
    public void onWired() {
        try {
            MH_CONTAINER_CONFIGURATION_ON_WIRE.invokeExact((ComponentConfiguration) beanHandle.configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        super.onWired();
    }

    public BeanSetup(ContainerSetup container, RealmSetup realm, LifetimeSetup lifetime, PackedBeanMaker<?> beanHandle) {
        super(container.application, realm, lifetime, container);
        this.beanType = BeanType.CONTAINER_BEAN;
        this.factory = beanHandle.factory;
        this.hookModel = beanHandle.hookModel;
        this.beanHandle = beanHandle;
        this.singletonHandle = beanHandle.kind == BeanType.CONTAINER_BEAN ? lifetime.pool.reserve(beanHandle.beanType) : null;

        if (realm instanceof ExtensionRealmSetup s) {
            container.useExtensionSetup(s.realmType(), null).beans.beans.put(Key.of(beanHandle.beanType), this);
        }

        if (factory == null) {
            // We already have a bean instance, no need to have an injection node for creating a new bean instance.
            this.dependencyNode = null;

            // Store the supplied bean instance in the lifetime (constant) pool.
            // Skal vel faktisk vaere i application poolen????
            // Ja der er helt sikker forskel paa noget der bliver initializeret naar containeren bliver initialiseret
            // og saa constant over hele applikation.
            // Skal vi overhoved have en constant pool???
            lifetime.pool.addConstant(pool -> singletonHandle.store(pool, beanHandle.source));
            // Or maybe just bind the instance directly in the method handles.
        } else {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<InternalDependency> dependencies = (List) factory.dependenciesOld();

            // Extract a MethodHandlefrom the factory
            MethodHandle mh = realm.accessor().toMethodHandle(factory);

            this.dependencyNode = new BeanInstanceDependencyNode(this, dependencies, mh);

            container.beans.addConsumer(dependencyNode);
        }

        // Find a hook model for the bean type and wire it
        hookModel.onWire(this);

        // Set the name of the component if it have not already been set using a wirelet
        initializeNameWithPrefix(hookModel.simpleName());
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

    /** {@inheritDoc} */
    @Override
    public BeanMirror mirror() {
        return new BuildTimeBeanMirror();
    }

    public ServiceSetup provide() {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceSetup s = service;
        if (s == null) {
           // parent.beans.getServiceManagerOrCreate();
           s = service = parent.beans.getServiceManagerOrCreate().provideSource(this, defaultKey());
        }
        return s;
    }

    public <T> void sourceExport() {
        sourceProvide();
        parent.beans.getServiceManagerOrCreate().exports().export(service);
    }

    public void sourceProvide() {
        realm.checkOpen();
        provide();
    }

    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        realm.checkOpen();
        provide().as(key);
    }

    /** A build-time bean mirror. */
    public final class BuildTimeBeanMirror extends AbstractBuildTimeComponentMirror implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanType() {
            return hookModel.clazz;
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
        public Set<BeanElementMirror> hooks() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public <T extends BeanElementMirror> Set<?> hooks(Class<T> hookType) {
            return null;
        }

        @Override
        public BeanKind kind() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension<?>>> registrant() {
            throw new UnsupportedOperationException();
        }
    }
}
