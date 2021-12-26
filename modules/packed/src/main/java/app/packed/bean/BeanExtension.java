package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.container.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.service.ServiceBeanConfiguration;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.PackedBeanDriver;
import packed.internal.bean.PackedBeanDriverBinder;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.RealmSetup;

/**
 * An extension for creating new beans.
 */
public class BeanExtension extends Extension {

    /** The container we installing beans into. */
    final ContainerSetup parent;

    /**
     * Create a new bean extension.
     * 
     * @param configuration
     *            an extension configuration object
     */
    /* package-private */ BeanExtension(ExtensionConfiguration configuration) {
        this.parent = ((ExtensionSetup) configuration).container;
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
        //// Maaske har vi noget install(BeanDriver bd, Class), installInstance(BeanDriver, Object)
        //// IDK maaske kun paa support

        PackedBeanDriver<ContainerBeanConfiguration<T>> driver = PackedBeanDriverBinder.ofSingleton(implementation);
        return wire(driver, parent, parent.realm);
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
        PackedBeanDriver<ContainerBeanConfiguration<T>> driver = PackedBeanDriverBinder.ofSingleton(factory);
        return wire(driver, parent, parent.realm);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this container will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link ServiceBeanConfiguration} can be used to specify a specific parent.
     *
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    public <T> ContainerBeanConfiguration<T> installInstance(T instance) {
        PackedBeanDriver<ContainerBeanConfiguration<T>> driver = PackedBeanDriverBinder.ofSingletonInstance(instance);
        return wire(driver, parent, parent.realm);
    }

    /** {@inheritDoc} */
    @Override
    protected BeanExtensionMirror mirror() {
        return mirrorInitialize(new BeanExtensionMirror(this));
    }

    static final <C extends BeanConfiguration<?>> C wire(PackedBeanDriver<C> driver, ContainerSetup parent, RealmSetup realm, Wirelet... wirelets) {
        requireNonNull(driver, "driver is null");
        // Prepare to wire the component (make sure the realm is still open)
        realm.wirePrepare();

        // Create the new component
        BeanSetup component = new BeanSetup(parent.lifetime, realm, driver, parent, driver.binding);

        realm.wireCommit(component);

        // Return a component configuration to the user
        return driver.toConfiguration(component);
    }
}
