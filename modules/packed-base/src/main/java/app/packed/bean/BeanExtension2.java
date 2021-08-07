package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentOwner;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.service.ServiceBeanConfiguration;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

/**
 * An extension used for installing beans.
 * Two main types of functionality
 * 
 * Controls the lifecycle of bean instances
 * 
 * Supports bean member (Constructor, Method, Field) injection.
 * 
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
     * Installs a new application bean that will instantiate and wrap a single instance of the specified {@link Class} when
     * the application is initialized.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param <T>
     *            the type of bean
     * @param implementation
     *            the type of bean that will be installed
     * @return the configuration of the bean
     * @see BaseAssembly#install(Class)
     */
    public <T> ApplicationBeanConfiguration<T> install(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return wire(new ApplicationBeanConfiguration<>(), ComponentOwner.user(), implementation);
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
        return wire(new ApplicationBeanConfiguration<>(), ComponentOwner.user(), factory);
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
        return wire(new ApplicationBeanConfiguration<>(), ComponentOwner.user(), instance);
    }

    private static void checkInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }
    }

    <B extends BeanConfiguration<?>> B wire(B configuration, ComponentOwner owner, Object source) {

        return configuration;
    }

    public static class Sub extends Subtension {
        final BeanExtension2 extension;
        final ComponentOwner agent;

        Sub(BeanExtension2 extension, ComponentOwner agent) {
            this.extension = extension;
            this.agent = agent;
        }

        // Beans where the owner is itself

        public final <T> ApplicationBeanConfiguration<T> install(Class<T> implementation) {
            requireNonNull(implementation, "implementation is null");
            return extension.wire(new ApplicationBeanConfiguration<>(), agent, implementation);
        }

        public final <T, B extends BeanConfiguration<T>> B register(ComponentOwner agent, BeanDriver driver, B configuration, Class<T> implementation) {
            requireNonNull(implementation, "implementation is null");
            extension.wire(configuration, agent, implementation);
            throw new UnsupportedOperationException();
        }

        public final <T, B extends BeanConfiguration<T>> B registerChild(ComponentOwner agent, ComponentConfiguration parent, BeanDriver driver,
                B configuration, Class<T> implementation) {
            throw new UnsupportedOperationException();
        }

        public final MethodHandle accessor(ApplicationBeanConfiguration<?> configuration) {
            throw new UnsupportedOperationException();
        }

        // Must have been created by this subtension
        // (ExtensionContext) -> Object
        public final MethodHandle newInstance(UnmanagedBeanConfiguration<?> configuration) {
            return newInstanceBuilder(configuration).build();
        }

        public final MethodHandleBuilder newInstanceBuilder(UnmanagedBeanConfiguration<?> configuration) {
            throw new UnsupportedOperationException();
        }

        public final MethodHandle processor(ManagedBeanConfiguration<?> configuration) {
            throw new UnsupportedOperationException();
        }
    }
}
