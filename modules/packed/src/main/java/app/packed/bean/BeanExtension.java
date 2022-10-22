package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.bean.BeanHandle.InstallOption;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.ExtensionPoint.UseSite;
import app.packed.lifetime.RunState;
import app.packed.operation.InvocationType;
import app.packed.operation.Op;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.LifetimeOp;
import internal.app.packed.bean.MethodIntrospector;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.op.PackedOp;

/**
 * An extension that is used for installing new beans into a container.
 * <p>
 * All containers use this extension either directly or indirectly. As every container either defines at least 1 bean.
 * Or has a container descendants who does.
 */
public class BeanExtension extends Extension<BeanExtension> {

    /** The container we are installing beans into. */
    final ContainerSetup container;

    /** The internal configuration of the extension. */
    final ExtensionSetup extensionSetup = ExtensionSetup.crack(this);

    /** Create a new bean extension. */
    BeanExtension() {
        container = extensionSetup.container;
    }

    // Ahh er det lidt irriterende. Naar vi skal til at lave synthetisk beans taenker jeg?
    BeanSetup install(BeanKind kind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source, BeanExtensionPoint extensionPoint, UseSite useSite,
            BeanHandle.InstallOption... options) {
        ExtensionSetup installingExtension = extensionPoint == null ? extensionSetup : extensionPoint.usedByExtension();
//        PackedExtensionPointContext c = ((PackedExtensionPointContext) useSite);
        return BeanSetup.install(installingExtension, kind, beanClass, sourceKind, source, useSite, options);
    }

    <T> BeanHandle<T> installh(BeanKind kind, Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source, BeanExtensionPoint extensionPoint,
            UseSite useSite, BeanHandle.InstallOption... options) {
        ExtensionSetup installingExtension = extensionPoint == null ? extensionSetup : extensionPoint.usedByExtension();
//        PackedExtensionPointContext c = ((PackedExtensionPointContext) useSite);
        return new BeanHandle<>(BeanSetup.install(installingExtension, kind, beanClass, sourceKind, source, useSite, options));
    }

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
        requireNonNull(implementation, "implementation is null");

        // BeanHandle bh = container.prepare(BeanKind.Container).install(implementation);
        
        // Install the bean
        BeanSetup bean = install(BeanKind.CONTAINER, implementation, BeanSourceKind.CLASS, implementation, null, null);

        // return a bean configuration
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
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
        PackedOp<?> pop = PackedOp.crack(op);

        // Install the bean
        BeanSetup bean = install(BeanKind.CONTAINER, pop.type().returnType(), BeanSourceKind.OP, pop, null, null);

        // return a bean configuration
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
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
        requireNonNull(instance, "instance is null");

        // Install the bean
        BeanSetup bean = install(BeanKind.CONTAINER, instance.getClass(), BeanSourceKind.INSTANCE, instance, null, null);

        // return a bean configuration
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Class<T> implementation) {
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.assembly, null, BeanKind.LAZY, implementation);
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Op<T> op) {
        PackedOp<?> pop = PackedOp.crack(op);

        // Install the bean
        BeanSetup bean = install(BeanKind.LAZY, pop.type().returnType(), BeanSourceKind.OP, pop, null, null);

        // return a bean configuration
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    /**
     * Installs a new {@link BeanKind#STATIC static} bean.
     * 
     * @param implementation
     *            the static bean class
     * @return a configuration for the bean
     * @throws DublicateBeanClassException
     *             if there other beans of the same type that has already been installed
     * 
     * @see BeanKind#STATIC
     * @see BeanSourceKind#CLASS
     */
    public BeanConfiguration installStatic(Class<?> implementation) {
        requireNonNull(implementation, "implementation is null");

        // Install the bean
        BeanSetup bean = install(BeanKind.STATIC, implementation, BeanSourceKind.CLASS, implementation, null, null);

        // return a bean configuration
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    /**
     * @see BeanKind#CONTAINER
     * @see BeanSourceKind#CLASS
     * @see BeanHandle.InstallOption#multiInstall()
     */
    public <T> ProvideableBeanConfiguration<T> multiInstall(Class<T> implementation) {
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.assembly, null, BeanKind.CONTAINER, implementation, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstall(Op<T> op) {
        BeanSetup bean = BeanSetup.installOp(extensionSetup, container.assembly, null, BeanKind.CONTAINER, op, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallInstance(T instance) {
        BeanSetup bean = BeanSetup.installInstance(extensionSetup, container.assembly, null, instance, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Class<T> implementation) {
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.assembly, null, BeanKind.LAZY, implementation, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Op<T> op) {
        BeanSetup bean = BeanSetup.installOp(extensionSetup, container.assembly, null, BeanKind.LAZY, op, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
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
            public void onMethod(OnMethod method) {
                AnnotationReader ar = method.annotations();

                if (ar.isAnnotationPresent(OnInitialize.class)) {
                    @SuppressWarnings("unused")
                    OnInitialize oi = ar.readRequired(OnInitialize.class);
                    OperationSetup os = ((MethodIntrospector) method).newOperation(extensionSetup, InvocationType.defaults());
                    os.bean.lifetimeOperations.add(new LifetimeOp(RunState.INITIALIZING, os));
                    os.bean.operations.add(os);
                }

                if (ar.isAnnotationPresent(OnStart.class)) {
                    @SuppressWarnings("unused")
                    OnStart oi = ar.readRequired(OnStart.class);
                    OperationSetup os = ((MethodIntrospector) method).newOperation(extensionSetup, InvocationType.defaults());
                    os.bean.lifetimeOperations.add(new LifetimeOp(RunState.STARTING, os));
                    os.bean.operations.add(os);
                }

                if (ar.isAnnotationPresent(OnStop.class)) {
                    @SuppressWarnings("unused")
                    OnStop oi = ar.readRequired(OnStop.class);
                    OperationSetup os = ((MethodIntrospector) method).newOperation(extensionSetup, InvocationType.defaults());
                    os.bean.lifetimeOperations.add(new LifetimeOp(RunState.STOPPING, os));
                    os.bean.operations.add(os);
                }

                if (ar.isAnnotationPresent(Inject.class)) {
                    ((MethodIntrospector) method).newOperation(extensionSetup, InvocationType.defaults());
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

    /** {@inheritDoc} */
    @Override
    protected void onAssemblyClose() {
        container.injectionManager.resolve();
    }
}
