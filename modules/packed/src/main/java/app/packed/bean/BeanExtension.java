package app.packed.bean;

import app.packed.bean.BeanExtensionPoint.BeanInstaller;
import app.packed.container.BaseAssembly;
import app.packed.container.FrameworkExtension;
import app.packed.lifetime.RunState;
import app.packed.operation.Op;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.LifetimeOperation;
import internal.app.packed.operation.OperationSetup;

/**
 * An extension that is used for installing new beans into a container.
 * <p>
 * All containers use this extension either directly or indirectly. As every container either defines at least 1 bean.
 * Or has a container descendants who does.
 */
public class BeanExtension extends FrameworkExtension<BeanExtension> {

    /** The internal configuration of the extension. */
    final ExtensionSetup extensionSetup = ExtensionSetup.crack(this);

    /** Create a new bean extension. */
    BeanExtension() {}

    /**
     * Installs a bean that will use the specified {@link Class} to instantiate a single instance of the bean when the
     * application is initialized.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of bean to install
     * @return the configuration of the bean
     * @see BaseAssembly#install(Class)
     */
    public <T> ProvideableBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).install(implementation);
        return new ProvideableBeanConfiguration<>(handle);
    }

    /**
     * Installs a component that will use the specified {@link Op} to instantiate the component instance.
     * 
     * @param op
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Op)
     */
    public <T> ProvideableBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).install(op);
        return new ProvideableBeanConfiguration<>(handle);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this container will have have component as its
     * parent.
     *
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    public <T> ProvideableBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).installInstance(instance);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Class<T> implementation) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).install(implementation);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).install(op);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    /**
     * Installs a new {@link BeanKind#STATIC static} bean.
     * 
     * @param implementation
     *            the static bean class
     * @return a configuration for the bean
     * 
     * @see BeanKind#STATIC
     * @see BeanSourceKind#CLASS
     */
    public BeanConfiguration installStatic(Class<?> implementation) {
        BeanHandle<?> handle = newInstaller(BeanKind.STATIC).install(implementation);
        return new BeanConfiguration(handle);
    }

    /**
     * @see BeanKind#CONTAINER
     * @see BeanSourceKind#CLASS
     * @see BeanHandle.InstallOption#multiInstall()
     */
    public <T> ProvideableBeanConfiguration<T> multiInstall(Class<T> implementation) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).multiInstall().install(implementation);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> multiInstall(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).multiInstall().install(op);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallInstance(T instance) {
        BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER).multiInstall().installInstance(instance);
        return new ProvideableBeanConfiguration<>(handle);
    }

    // Skriv usecases naeste gang. Taenker over det hver gang
    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Class<T> implementation) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).multiInstall().install(implementation);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Op<T> op) {
        BeanHandle<T> handle = newInstaller(BeanKind.LAZY).multiInstall().install(op);
        return new ProvideableBeanConfiguration<>(handle); // Providable???
    }

    /**
     * Creates a new BeanIntrospector for handling annotations managed by BeanExtension.
     * 
     * @see Inject
     * @see OnInitialize
     * @see OnStart
     * @see OnStop
     */
    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void onField(OnField field) {
                if (field.annotations().isAnnotationPresent(Inject.class)) {

                }
            }

            @Override
            public void onMethod(OnMethod method) {
                AnnotationReader ar = method.annotations();

                if (ar.isAnnotationPresent(OnInitialize.class)) {
                    @SuppressWarnings("unused")
                    OnInitialize oi = ar.readRequired(OnInitialize.class);
                    OperationSetup os = OperationSetup.crack(method.newOperation());
                    os.bean.operationsLifetime.add(new LifetimeOperation(RunState.INITIALIZING, os));
                }

                if (ar.isAnnotationPresent(OnStart.class)) {
                    @SuppressWarnings("unused")
                    OnStart oi = ar.readRequired(OnStart.class);
                    OperationSetup os = OperationSetup.crack(method.newOperation());
                    os.bean.operationsLifetime.add(new LifetimeOperation(RunState.STARTING, os));
                }

                if (ar.isAnnotationPresent(OnStop.class)) {
                    @SuppressWarnings("unused")
                    OnStop oi = ar.readRequired(OnStop.class);
                    OperationSetup os = OperationSetup.crack(method.newOperation());
                    os.bean.operationsLifetime.add(new LifetimeOperation(RunState.STOPPING, os));
                }

                if (ar.isAnnotationPresent(Inject.class)) {
                    OperationSetup.crack(method.newOperation());
                }
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    protected BeanExtensionMirror newExtensionMirror() {
        return new BeanExtensionMirror();
    }

    /** {@inheritDoc} */
    @Override
    protected BeanExtensionPoint newExtensionPoint() {
        return new BeanExtensionPoint();
    }

    BeanInstaller newInstaller(BeanKind kind) {
        return new PackedBeanInstaller(extensionSetup, kind, null);
    }
}
