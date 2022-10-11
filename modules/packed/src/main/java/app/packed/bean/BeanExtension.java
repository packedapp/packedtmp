package app.packed.bean;

import app.packed.bean.BeanExtensionPoint.InstallOption;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.operation.InvocationType;
import app.packed.operation.Op;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.BeanMethodIntrospector;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;

/**
 * A bean extension is used for installing beans into a container.
 * <p>
 * All containers use this extension. As every container either defines at least 1 bean. Or has a container descendants
 * who does.
 */
public class BeanExtension extends Extension<BeanExtension> {

    /** The container we are installing beans into. */
    final ContainerSetup container;

    final ExtensionSetup extensionSetup = ExtensionSetup.crack(this);

    /** Create a new bean extension. */
    BeanExtension() {
        container = extensionSetup.container;
    }

    void filter(BaseAssembly.Linker l) {

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
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.realm, null, BeanKind.CONTAINER, implementation);
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }


    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void onMethodHook(OnMethod method) {
                ((BeanMethodIntrospector) method).newOperation(extensionSetup, InvocationType.defaults());
            }
        };
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
        BeanSetup bean = BeanSetup.installOp(extensionSetup, container.realm, null, BeanKind.CONTAINER, op);
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
        BeanSetup bean = BeanSetup.installInstance(extensionSetup, container.realm, null, instance);
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Class<T> implementation) {
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.realm, null, BeanKind.LAZY, implementation);
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> installLazy(Op<T> op) {
        BeanSetup bean = BeanSetup.installOp(extensionSetup, container.realm, null, BeanKind.LAZY, op);
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    /**
     * Installs a new {@link BeanKind#STATIC static} bean.
     * 
     * @param implementation
     *            the static bean class
     * @return a configuration for the bean
     * @throws MultipleBeanOfSameTypeDefinedException
     *             if there other beans of the same type that has already been installed
     * 
     * @see BeanKind#STATIC
     * @see BeanSourceKind#CLASS
     */
    public BeanConfiguration installStatic(Class<?> implementation) {
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.realm, null, BeanKind.STATIC, implementation);
        return new BeanConfiguration(new BeanHandle<>(bean));
    }

    /**
     * @see BeanKind#CONTAINER
     * @see BeanSourceKind#CLASS
     * @see BeanHandle.InstallOption#multiInstall()
     */
    public <T> ProvideableBeanConfiguration<T> multiInstall(Class<T> implementation) {
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.realm, null, BeanKind.CONTAINER, implementation, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstall(Op<T> op) {
        BeanSetup bean = BeanSetup.installOp(extensionSetup, container.realm, null, BeanKind.CONTAINER, op, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallInstance(T instance) {
        BeanSetup bean = BeanSetup.installInstance(extensionSetup, container.realm, null, instance, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Class<T> implementation) {
        BeanSetup bean = BeanSetup.installClass(extensionSetup, container.realm, null, BeanKind.LAZY, implementation, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
    }

    public <T> ProvideableBeanConfiguration<T> multiInstallLazy(Op<T> op) {
        BeanSetup bean = BeanSetup.installOp(extensionSetup, container.realm, null, BeanKind.LAZY, op, InstallOption.multiInstall());
        return new ProvideableBeanConfiguration<>(new BeanHandle<>(bean));
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
