package packed.internal.component.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.BeanMirror;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.BuildSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.RealmAccessor;
import packed.internal.component.RealmSetup;
import packed.internal.hooks.usesite.BootstrappedClassModel;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProducer;
import packed.internal.inject.dependency.InjectionNode;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.lifetime.LifetimePool;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolAccessor;

/** The internal configuration of a bean. */
public final class BeanSetup extends ComponentSetup implements DependencyProducer{

    BeanSetup(BuildSetup build, LifetimeSetup lifetime, RealmSetup realm, PackedBeanDriver<?> driver, @Nullable ComponentSetup parent,
            Wirelet[] wirelets) {
        super(build, realm, lifetime, driver, parent, wirelets);

        
        // Reserve a place in the constant pool if the source is a singleton
        // If instance != null we kan vel pool.permstore()
        // BuildStore
        this.singletonAccessor = driver.isConstant ? lifetime.pool.reserve() : null;

        // A realm accessor that allows us to find all hooks a component source
        RealmAccessor accessor = realm.accessor();
        Object source = driver.binding;
        // The source is either a Class, a Factory, or a generic instance
        if (source instanceof Class<?> cl) {
            this.boundToinstance = null;
            // TODO fix
            boolean isStaticClassSource = false;
            this.factory = isStaticClassSource ? null : Factory.of(cl);

        } else if (source instanceof Factory<?> fac) {
            this.boundToinstance = null;
            this.factory = fac;
        } else {
            this.boundToinstance = source;
            this.factory = null;

            // non-constants singlestons are added to the constant pool elsewhere
            lifetime.pool.addConstant(pool -> singletonAccessor.store(pool, boundToinstance)); // writeToPool will be called later
        }

        if (factory == null) {
            this.injectionNode = null;
        } else {
            MethodHandle mh = accessor.toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.variables();
            this.injectionNode = new InjectionNode(this, dependencies, mh);
            container.injection.addNode(injectionNode);
        }

        // Find a hook model for the bean type and wire it
        this.hookModel = accessor.modelOf(driver.beanType());
        hookModel.onWire(this);
        
        // Set the name of the component if it have not already been set using a wirelet
        if (name == null) {
            initializeNameWithPrefix(hookModel.simpleName());
        }
    }


    /** If the source represents a constant. */
    @Nullable
    private final Object boundToinstance;

    /**
     * Factory that was specified. We only keep this around to find the key that it should be exposed as a service with. As
     * we need to lazy calculate it from {@link #provide(ComponentSetup)}
     */
    @Nullable
    private final Factory<?> factory;

    /** A model of every hook on the source. */
    public final BootstrappedClassModel hookModel; // contains provided stuff

    /** An injection node, if instances of the source needs to be created at runtime (not a constant). */
    @Nullable
    private final InjectionNode injectionNode;

    /** A service object if the source is provided as a service. */
    @Nullable
    public ServiceSetup service;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    public final PoolAccessor singletonAccessor;

    /** {@inheritDoc} */
    @Override
    @Nullable
    public InjectionNode dependant() {
        return injectionNode;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // Must return MethodHandle(ConstantPool)T
        if (boundToinstance != null) {
            // Jeg tror faktisk godt vi vil returnere den for array'et istedet for...
            MethodHandle mh = MethodHandles.constant(boundToinstance.getClass(), boundToinstance); // MethodHandle()T
            mh = MethodHandles.dropArguments(mh, 0, LifetimePool.class); // MethodHandle()T -> // MethodHandle(LifetimePool)T
            return mh;
        } else if (singletonAccessor != null) {
            return singletonAccessor.indexedReader(hookModel.clazz);
        } else {
            return injectionNode.buildMethodHandle();
        }
    }

    public ServiceSetup provide() {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceSetup s = service;
        if (s == null) {
            Key<?> key;
            if (factory != null) {
                key = factory.key();
            } else {
                key = Key.of(hookModel.clazz); // Move to model?? What if instance has Qualifier???
            }
            s = service = container.injection.getServiceManagerOrCreate().provideSource(this, key);
        }
        return s;
    }
    
    /** {@inheritDoc} */
    @Override
    public BeanMirror mirror() {
        return new BuildTimeBeanMirror();
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) container.injection.getServiceManagerOrCreate().exports().export(service);
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
    private final class BuildTimeBeanMirror extends ComponentSetup.BuildTimeComponentMirror implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanType() {
            return hookModel.clazz;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension>> driverExtension() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Set<?> hooks() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public <T> Set<?> hooks(Class<T> hookType) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BeanMode mode() {
            throw new UnsupportedOperationException();
        }
    }
}
