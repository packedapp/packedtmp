package app.packed.bean;

import app.packed.bean.OldBeanDriver.BeanDriver;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Wirelet;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.inject.Factory;
import app.packed.service.ServiceBeanConfiguration;
import packed.internal.component.bean.PackedBeanDriver;
import packed.internal.component.bean.PackedBeanDriverBinder;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

/**
 * An extension used for installing beans.
 */
public class BeanExtension extends Extension {

    /** The service manager. */
    final ContainerSetup container;

    final ExtensionSetup extension;

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
    public BaseBeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        PackedBeanDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingleton(implementation);
        return container.wire(driver, container.realm, wirelets);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Factory)
     */
    public BaseBeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        PackedBeanDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingleton(factory);
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
    public BaseBeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        PackedBeanDriver<BaseBeanConfiguration> driver = PackedBeanDriverBinder.ofSingletonInstance(instance);
        return container.wire(driver, container.realm, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    protected ExtensionMirror<?> mirror() {
        return mirrorInitialize(new BeanExtensionMirror(this));
    }

    public final class Sub extends Subtension {

        public BaseBeanConfiguration inheritOrInstall(Class<?> implementation) {
            System.out.println(extension);
            throw new UnsupportedOperationException();
        }

        public BaseBeanConfiguration inheritOrInstall(Factory<?> implementation) {
            System.out.println(extension);
            throw new UnsupportedOperationException();
        }

        public final BaseBeanConfiguration install(Class<?> implementation) {
            throw new UnsupportedOperationException();
        }

        public final BaseBeanConfiguration install(Factory<?> factory) {
            throw new UnsupportedOperationException();
        }

        public final BaseBeanConfiguration installInstance(Object instance) {
            throw new UnsupportedOperationException();
        }

        public <T, C extends BeanConfiguration> C wire(BeanDriver<T, C> binder, Class<? extends T> implementation, Wirelet... wirelets) {
            PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;
            return extension.wire(b.bind(implementation), wirelets);
        }

        public <T, C extends BeanConfiguration> C wire(BeanDriver<T, C> binder, Factory<? extends T> implementation, Wirelet... wirelets) {
            PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;
            return extension.wire(b.bind(implementation), wirelets);
        }

        // installs a child to the specified component.
        // 1. Specifie ComponentConfiguration must be in the same container
        // 2. Specifie ComponentConfiguration must have been installed by the same extension
        public <T, C extends BeanConfiguration> C wireChild(ComponentConfiguration parent, BeanDriver<T, C> binder, Class<? extends T> implementation,
                Wirelet... wirelets) {
            PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;
            return extension.wire(b.bind(implementation), wirelets);
        }

        public <T, C extends BeanConfiguration> C wireInstance(BeanDriver<T, C> binder, T instance, Wirelet... wirelets) {
            PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;
            return extension.wire(b.bindInstance(instance), wirelets);
        }

        // Det er lidt for at undgaa BeanDriver<T,C>...
        public <B extends BeanConfiguration> B populateConfiguration(B beanConfiguration) {
            return beanConfiguration;
        }
    }
}
