package packed.internal.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
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
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.RealmSetup;
import packed.internal.hooks.usesite.BootstrappedClassModel;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProducer;
import packed.internal.inject.dependency.InjectionNode;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolAccessor;

/** The build-time configuration of a bean. */
public final class BeanSetup extends ComponentSetup implements DependencyProducer {

    /**
     * Factory that was specified. We only keep this around to find the key that it should be exposed as a service with. As
     * we need to lazy calculate it from {@link #provide(ComponentSetup)}
     */
    @Nullable
    private final Factory<?> factory;

    /** A model of every hook on the bean. */
    public final BootstrappedClassModel hookModel;

    /** An injection node, if instances of the source needs to be created at runtime (not a constant). */
    @Nullable
    private final InjectionNode injectionNode;

    /** A service object if the source is provided as a service. */
    // Would be nice if we could move it somewhere else.. Like Listener
    @Nullable
    private ServiceSetup service;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    @Nullable
    public final PoolAccessor singletonAccessor;

    public final BeanType beanType;

    public BeanSetup(ContainerSetup parent, RealmSetup realm, LifetimeSetup lifetime, PackedBeanHandle<?> beanHandle) {
        super(parent.application, realm, lifetime, parent);
        this.beanType = BeanType.BASE;
        this.factory = beanHandle.factory;
        this.singletonAccessor = beanHandle.kind == BeanType.BASE ? lifetime.pool.reserve(beanHandle.beanType) : null;
        
        if (factory == null) {
            lifetime.pool.addConstant(pool -> singletonAccessor.store(pool, beanHandle.source));
        }

        if (factory == null) {
            this.injectionNode = null;
        } else {
            MethodHandle mh = realm.accessor().toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.dependenciesOld();
            this.injectionNode = new InjectionNode(this, dependencies, mh);
            parent.injection.addNode(injectionNode);
        }

        // Find a hook model for the bean type and wire it
        this.hookModel = realm.accessor().modelOf(beanHandle.beanType);
        hookModel.onWire(this);

        // Set the name of the component if it have not already been set using a wirelet
        initializeNameWithPrefix(hookModel.simpleName());
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public InjectionNode dependant() {
        return injectionNode;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // If we have a singleton accessor return a method handle that can read the single bean instance
        // Otherwise return a method handle that can instantiate a new bean
        if (singletonAccessor != null) {
            return singletonAccessor.poolReader(); // MethodHandle(ConstantPool)T
        } else {
            return injectionNode.buildMethodHandle(); // MethodHandle(ConstantPool)T
        }
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
            Key<?> key;
            if (factory != null) {
                key = Key.convertTypeLiteral(factory.typeLiteral());
            } else {
                key = Key.of(hookModel.clazz); // Move to model?? What if instance has Qualifier???
            }
            s = service = parent.injection.getServiceManagerOrCreate().provideSource(this, key);
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) parent.injection.getServiceManagerOrCreate().exports().export(service);
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

    public Optional<Key<?>> sourceProvideAsKey() {
        return service == null ? Optional.empty() : Optional.of(service.key());
    }

    /** A build-time bean mirror. */
    public final class BuildTimeBeanMirror extends AbstractBuildTimeComponentMirror implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanType() {
            return hookModel.clazz;
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

        @Override
        public BeanKind kind() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<ComponentMirror> children() {
            return List.of();
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
    }
}
