package app.packed.container;

import java.util.Set;

import app.packed.component.BaseBeanConfiguration;
import app.packed.component.BeanDriver;
import app.packed.component.BeanDriver.Binder;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import packed.internal.container.ContainerSetup;

/**
 * <p>
 * Currently only a single concrete implementation exists.
 *  
 * but users are free to create other implementations that restrict the functionality
 * of the default container configuration by overridding this class.
 */
public abstract /* non-sealed */ class ContainerConfiguration extends ComponentConfiguration {
   
    // TODO pack into container()...
    ContainerSetup container;

    private Binder<Object, BaseBeanConfiguration> defaultBeanBinder() {
        return Binder.DEFAULT;
    }
    
    @Override
    protected ContainerMirror mirror() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Returns an unmodifiable view of the extensions that are currently used.
     * 
     * @return an unmodifiable view of the extensions that are currently used
     * 
     * @see #use(Class)
     * @see ContainerAssembly#extensions()
     * @see ContainerMirror#extensions()
     */
    protected Set<Class<? extends Extension>> extensions() {
        return container.extensions();
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
    protected BaseBeanConfiguration install(Class<?> implementation) {
        ComponentDriver<BaseBeanConfiguration> driver = defaultBeanBinder().bind(implementation);
        return wire(driver);
    }
    
    

    protected BaseBeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = defaultBeanBinder().bind(implementation);
        return wire(driver, wirelets);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see ContainerAssembly#install(Factory)
     */
    protected BaseBeanConfiguration install(Factory<?> factory) {
        ComponentDriver<BaseBeanConfiguration> driver = defaultBeanBinder().bind(factory);
        return wire(driver);
    }
    
    protected BaseBeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = defaultBeanBinder().bind(factory);
        return wire(driver, wirelets);
    }

    /**
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerAssembly#installInstance(Object)
     */
    protected BaseBeanConfiguration installInstance(Object instance) {
        ComponentDriver<BaseBeanConfiguration> driver = defaultBeanBinder().bindInstance(instance);
        return wire(driver);
    }

    protected BaseBeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        ComponentDriver<BaseBeanConfiguration> driver = defaultBeanBinder().bindInstance(instance);
        return wire(driver, wirelets);
    }

    /**
     * Installs a stateless component.
     * <p>
     * Extensions might still contain state. So Stateless is better under the assumption that extensions are better tested
     * the user code.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    protected BaseBeanConfiguration stateless(Class<?> implementation) {
        return wire(BeanDriver.Binder.driverStateless(implementation));
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
     * @see #extensions()
     */
    protected <T extends Extension> T use(Class<T> extensionType) {
        return container.useExtension(extensionType);
    }
}
