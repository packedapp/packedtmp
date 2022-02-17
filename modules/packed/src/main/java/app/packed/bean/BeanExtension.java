package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.component.UserOrExtension;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.service.ServiceLocator;
import packed.internal.bean.PackedBeanDriver;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.inject.service.runtime.AbstractServiceLocator;

/**
 * An extension for creating new beans.
 */
public class BeanExtension extends Extension<BeanExtension> {

    /** The container we installing beans into. */
    final ContainerSetup container;

    /**
     * Create a new bean extension.
     * 
     * @param configuration
     *            an extension configuration object
     */
    /* package-private */ BeanExtension(ExtensionConfiguration configuration) {
        this.container = ((ExtensionSetup) configuration).container;
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
    public <T> ContainerBeanConfiguration<T> install(Class<T> implementation) {
        PackedBeanDriver<T> handle = PackedBeanDriver.ofFactory(container, UserOrExtension.user(), BeanSupport.of(implementation));
        return new ContainerBeanConfiguration<>(handle);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Factory)
     */
    public <T> ContainerBeanConfiguration<T> install(Factory<T> factory) {
        PackedBeanDriver<T> handle = PackedBeanDriver.ofFactory(container, UserOrExtension.user(), factory);
        return new ContainerBeanConfiguration<>(handle);
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
    public <T> ContainerBeanConfiguration<T> installInstance(T instance) {
        PackedBeanDriver<T> handle = PackedBeanDriver.ofInstance(container, UserOrExtension.user(), instance);
        return new ContainerBeanConfiguration<>(handle);
    }

    /**
     * Provides every service from the specified locator.
     * 
     * @param locator
     *            the locator to provide services from
     * @throws IllegalArgumentException
     *             if the specified locator is not implemented by Packed
     */
    public void provideAll(ServiceLocator locator) {
        requireNonNull(locator, "locator is null");
        if (!(locator instanceof AbstractServiceLocator l)) {
            throw new IllegalArgumentException("Custom implementations of " + ServiceLocator.class.getSimpleName()
                    + " are currently not supported, locator type = " + locator.getClass().getName());
        }
        checkUserConfigurable();
        container.beans.getServiceManager().provideAll(l);
    }

    public <T> ProvidableBeanConfiguration<T> providePrototype(Class<T> implementation) {
        PackedBeanDriver<T> handle = PackedBeanDriver.ofFactory(container, UserOrExtension.user(), BeanSupport.of(implementation));
        handle.prototype();
        ProvidableBeanConfiguration<T> sbc = new ProvidableBeanConfiguration<T>(handle);
        return sbc.provide();
    }

    public <T> ProvidableBeanConfiguration<T> providePrototype(Factory<T> factory) {
        PackedBeanDriver<T> bh = PackedBeanDriver.ofFactory(container, UserOrExtension.user(), factory);
        bh.prototype();
        ProvidableBeanConfiguration<T> sbc = new ProvidableBeanConfiguration<T>(bh);
        return sbc.provide();
    }

    public int beanCount() {
        return 4;
    }

    @Override
    protected void onClose() {
        super.onClose();
    }

    @Override
    protected void onUserClose() {
        super.onUserClose();
    }

    /** {@inheritDoc} */
    @Override
    protected BeanExtensionMirror mirror() {
        return mirrorInitialize(new BeanExtensionMirror(tree()));
    }
//
//    static final <C extends BeanConfiguration<?>> C wire(PackedBeanDriver<C> driver, ContainerSetup parent, RealmSetup realm, Wirelet... wirelets) {
//        requireNonNull(driver, "driver is null");
//        // Prepare to wire the component (make sure the realm is still open)
//        realm.wirePrepare();
//
//        // Create the new component
//        BeanSetup component = new BeanSetup(parent.lifetime, realm, driver, parent, driver.binding);
//
//        realm.wireCommit(component);
//
//        // Return a component configuration to the user
//        return driver.toConfiguration(component);
//    }
}
