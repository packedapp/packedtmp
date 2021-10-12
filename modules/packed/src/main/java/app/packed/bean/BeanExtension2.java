package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.BaseAssembly;
import app.packed.component.Operator;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.service.ServiceBeanConfiguration;
import packed.internal.bundle.BundleSetup;
import packed.internal.bundle.ExtensionSetup;

/**
 * An extension used for installing beans. Two main types of functionality
 * 
 * Controls the lifecycle of bean instances
 * 
 * Supports bean member (Constructor, Method, Field) injection.
 * 
 */
public class BeanExtension2 extends Extension {

    /** The bundle we are registering the beans in. */
    final BundleSetup bundle;

    /**
     * Create a new bean extension.
     * 
     * @param configuration
     *            an extension configuration object.
     */
    /* package-private */ BeanExtension2(ExtensionConfiguration configuration) {
        this.bundle = ((ExtensionSetup) configuration).bundle;
    }

    /**
     * Installs a new bean of the specified type with this extension's bundle as the parent component. A single instance of
     * the specified class will be instantiated together with the container this extension's bundle is a part of
     * 
     * that will instantiate and a instance of the specified {@link Class} when the container is initialized.
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
    public <T> ContainerBeanConfiguration<T> install(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return wire(new ContainerBeanConfiguration<>(), Operator.application(), implementation);
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
        requireNonNull(factory, "factory is null");
        return wire(new ContainerBeanConfiguration<>(), Operator.application(), factory);
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
        checkIsProperInstance(instance);
        return wire(new ContainerBeanConfiguration<>(), Operator.application(), instance);
    }

    <B extends BeanConfiguration<?>> B wire(B configuration, Operator owner, Object source) {

        return configuration;
    }

    /**
     * Checks that the specified instance object is not a instance of {@link Class} or {@link Factory}.
     * 
     * @param instance
     *            the object to check
     */
    private static void checkIsProperInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }
    }
}
