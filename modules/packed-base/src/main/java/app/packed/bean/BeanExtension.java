package app.packed.bean;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.BaseAssembly;
import app.packed.container.CommonContainerAssembly;
import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import app.packed.inject.Factory;
import app.packed.service.ServiceBeanConfiguration;
import packed.internal.component.bean.PackedBeanDriverBinder;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

public class BeanExtension extends Extension {

    /** The service manager. */
    private final ContainerSetup container;

    private final ExtensionSetup extension;

    /**
     * Create a new bean extension.
     * 
     * @param setup
     *            an extension setup object (hidden).
     */
    /* package-private */ BeanExtension(ExtensionSetup extension) {
        this.extension = extension;
        this.container = extension.container;
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    // add? i virkeligheden wire vi jo class komponenten...
    // Og taenker, vi har noget a.la. configuration().wire(ClassComponent.Default.bind(implementation))
    public BaseBeanConfiguration install(Class<?> implementation) {
        ComponentDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingleton(implementation);
        return container.wire(driver, container.realm);
    }

    /**
     * Installs a singleton bean that will use the specified {@link Factory} to instantiate the bean instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    public BaseBeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingleton(implementation);
        return container.wire(driver, container.realm, wirelets);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see BaseAssembly#install(Factory)
     */
    public BaseBeanConfiguration install(Factory<?> factory) {
        ComponentDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingleton(factory);
        return container.wire(driver, container.realm);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see CommonContainerAssembly#install(Factory)
     */
    public BaseBeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingleton(factory);
        return container.wire(driver, container.realm, wirelets);
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
    public BaseBeanConfiguration installInstance(Object instance) {
        ComponentDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingletonInstance(instance);
        return container.wire(driver, container.realm);
    }

    public BaseBeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingletonInstance(instance);
        return container.wire(driver, container.realm, wirelets);
    }

    public final class Sub extends Subtension {

        public BaseBeanConfiguration inheritOrInstall(Class<?> implementation) {
            System.out.println(extension);
            throw new UnsupportedOperationException();
        }

        public final BaseBeanConfiguration extInstall(Class<?> implementation) {
            throw new UnsupportedOperationException();
        }

        public final BaseBeanConfiguration extInstall(Factory<?> factory) {
            throw new UnsupportedOperationException();
        }

        public final BaseBeanConfiguration extInstall(Object instance) {
            throw new UnsupportedOperationException();
        }

        /**
         * 
         * @param <C>
         *            the type of component configuration that is being returned to the user
         * @param driver
         *            the component driver created by the extension
         * @param wirelets
         *            optional wirelets provided by the user (or the extension itself)
         * @return a component configuration object that can be returned to the user
         * @throws InternalExtensionException
         *             if the specified driver is not created by the extension itself
         */
        public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
            return extension.wire(driver, wirelets);
        }

    }
}
