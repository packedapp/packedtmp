package app.packed.bean;

import app.packed.component.UserOrExtension;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.service.ServiceBeanConfiguration;
import packed.internal.bean.PackedBeanHandle;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

/**
 * An extension used for installing beans. Two main types of functionality
 * 
 * Controls the lifecycle of bean instances
 * 
 * Supports bean member (Constructor, Method, Field) injection.
 * 
 */
public class BeanExtension2 extends Extension<BeanExtension2> {

    /** The container we are registering the beans in. */
    final ContainerSetup container;

    /**
     * Create a new bean extension.
     * 
     * @param configuration
     *            an extension configuration object.
     */
    /* package-private */ BeanExtension2(ExtensionConfiguration configuration) {
        this.container = ((ExtensionSetup) configuration).container;
    }

    /**
     * Installs a new bean of the specified type with this extension's container as the parent component. A single instance of
     * the specified class will be instantiated together with the container this extension's container is a part of
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
        PackedBeanHandle<T> handle = PackedBeanHandle.ofFactory(container, UserOrExtension.user(), implementation);
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
        PackedBeanHandle<T> handle = PackedBeanHandle.ofFactory(container, UserOrExtension.user(), factory);
        return new ContainerBeanConfiguration<>(handle);
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
        PackedBeanHandle<T> handle = PackedBeanHandle.ofInstance(container, UserOrExtension.user(), instance);
        return new ContainerBeanConfiguration<>(handle);
    }
}
