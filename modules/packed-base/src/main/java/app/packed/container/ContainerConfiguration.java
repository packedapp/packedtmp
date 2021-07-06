package app.packed.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Set;

import app.packed.component.BaseBeanConfiguration;
import app.packed.component.BeanDriver;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.service.ServiceBeanConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * <p>
 * Currently only a single concrete implementation exists.
 * 
 * but users are free to create other implementations that restrict the functionality of the default container
 * configuration by overridding this class.
 */
public abstract non-sealed class ContainerConfiguration extends ComponentConfiguration {

    /** A handle that can access superclass private ComponentConfiguration#component(). */
    private static final MethodHandle MH_COMPONENT_CONFIGURATION_COMPONENT = MethodHandles.explicitCastArguments(
            LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class, "component", ComponentSetup.class),
            MethodType.methodType(ContainerSetup.class, ContainerConfiguration.class));

    /** {@return the container setup instance that we are wrapping.} */
    private ContainerSetup container() {
        try {
            return (ContainerSetup) MH_COMPONENT_CONFIGURATION_COMPONENT.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * Returns an unmodifiable view of the extensions that are currently used.
     * 
     * @return an unmodifiable view of the extensions that are currently used
     * 
     * @see #use(Class)
     * @see CommonContainerAssembly#extensions()
     * @see ContainerMirror#extensions()
     */
    protected Set<Class<? extends Extension>> extensions() {
        return container().extensions();
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
    protected BaseBeanConfiguration install(Class<?> implementation) {
        ComponentDriver<BaseBeanConfiguration> driver = BeanDriver.ofSingleton(implementation);
        ComponentSetup component = container();
        return component.wire(driver, component.realm);
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
    protected BaseBeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = BeanDriver.ofSingleton(implementation);
        ComponentSetup component = container();
        return component.wire(driver, component.realm, wirelets);
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
    protected BaseBeanConfiguration install(Factory<?> factory) {
        ComponentDriver<BaseBeanConfiguration> driver = BeanDriver.ofSingleton(factory);
        ComponentSetup component = container();
        return component.wire(driver, component.realm);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see CommonContainerAssembly#install(Factory)
     */
    protected BaseBeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = BeanDriver.ofSingleton(factory);
        ComponentSetup component = container();
        return component.wire(driver, component.realm, wirelets);
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
    protected BaseBeanConfiguration installInstance(Object instance) {
        ComponentDriver<BaseBeanConfiguration> driver = BeanDriver.ofSingletonInstance(instance);
        ComponentSetup component = container();
        return component.wire(driver, component.realm);
    }

    protected BaseBeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = BeanDriver.ofSingletonInstance(instance);
        ComponentSetup component = container();
        return component.wire(driver, component.realm, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    protected ContainerMirror mirror() {
        return container().mirror();
    }

    /**
     * Returns an extension of the specified type. If this is the first time an extension of the specified type has been
     * requested. This method will create a new instance of the extension. This instance will be returned for all subsequent
     * calls to this method with the same extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if this configuration is no longer configurable and an extension of the specified type has not already
     *             been installed
     * @see #extensions()s
     */
    protected <T extends Extension> T use(Class<T> extensionType) {
        return container().useExtension(extensionType);
    }
}
