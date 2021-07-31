package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentConfiguration;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.service.ServiceBeanConfiguration;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

/**
 * An extension used for installing beans.
 */
public class BeanExtension2 extends Extension {

    /** The service manager. */
    final ContainerSetup container;

    final ExtensionSetup extension;

    /**
     * Create a new bean extension.
     * 
     * @param setup
     *            an extension setup object (hidden).
     */
    /* package-private */ BeanExtension2(ExtensionSetup extension) {
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
    public <T> ApplicationBeanConfiguration<T> install(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return wire(new ApplicationBeanConfiguration<>(), Registrant.user(), implementation);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the bean
     * @see CommonContainerAssembly#install(Factory)
     */
    public <T> ApplicationBeanConfiguration<T> install(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return wire(new ApplicationBeanConfiguration<>(), Registrant.user(), factory);
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
    public <T> ApplicationBeanConfiguration<T> installInstance(T instance) {
        checkInstance(instance);
        return wire(new ApplicationBeanConfiguration<>(), Registrant.user(), instance);
    }

    private static void checkInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }
    }

    <B extends BeanConfiguration<?>> B wire(B configuration, Registrant agent, Object source) {

        return configuration;
    }

    public static class Sub extends Subtension {
        final BeanExtension2 extension;
        final Registrant agent;

        Sub(BeanExtension2 extension, Registrant agent) {
            this.extension = extension;
            this.agent = agent;
        }

        public final <T> ApplicationBeanConfiguration<T> install(Class<T> implementation) {
            requireNonNull(implementation, "implementation is null");
            return extension.wire(new ApplicationBeanConfiguration<>(), agent, implementation);
        }

        public final <T, B extends BeanConfiguration<T>> B register(Registrant agent, BeanDriver driver, B configuration, Class<T> implementation) {
            requireNonNull(implementation, "implementation is null");
            return extension.wire(configuration, agent, implementation);
        }

        public final <T, B extends BeanConfiguration<T>> B registerChild(Registrant agent, ComponentConfiguration parent, BeanDriver driver, B configuration,
                Class<T> implementation) {
            throw new UnsupportedOperationException();
        }
    }
}
