package app.packed.container;

import java.util.Set;

import app.packed.component.Assembly;
import app.packed.component.BeanBinder;
import app.packed.component.BeanConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentMirror;
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
public abstract /* non-sealed */ class AbstractContainerConfiguration extends ComponentConfiguration {
   
    ContainerSetup container;

    private BeanBinder<Object, BeanConfiguration> defaultClassComponentDriver() {
        return BeanBinder.DEFAULT;
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
    protected BeanConfiguration install(Class<?> implementation) {
        ComponentDriver<BeanConfiguration> driver = defaultClassComponentDriver().bind(implementation);
        return wire(driver);
    }
    
    

    protected BeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        ComponentDriver<BeanConfiguration> driver = defaultClassComponentDriver().bind(implementation);
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
    protected BeanConfiguration install(Factory<?> factory) {
        ComponentDriver<BeanConfiguration> driver = defaultClassComponentDriver().bind(factory);
        return wire(driver);
    }
    
    protected BeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        ComponentDriver<BeanConfiguration> driver = defaultClassComponentDriver().bind(factory);
        return wire(driver, wirelets);
    }

    /**
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerAssembly#installInstance(Object)
     */
    protected BeanConfiguration installInstance(Object instance) {
        ComponentDriver<BeanConfiguration> driver = defaultClassComponentDriver().bindInstance(instance);
        return wire(driver);
    }

    protected BeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        ComponentDriver<BeanConfiguration> driver = defaultClassComponentDriver().bindInstance(instance);
        return wire(driver, wirelets);
    }

    /**
     * Links the specified assembly with this container as its parent.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a model of the component that was linked
     */
    @Override
    protected ComponentMirror link(Assembly<?> assembly, Wirelet... wirelets) {
        return super.link(assembly, wirelets);
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
    protected BeanConfiguration stateless(Class<?> implementation) {
        return wire(BeanBinder.driverStateless(implementation));
    }

    /**
     * Returns an extension of the specified type. If this is the first time an extension of the specified type has been
     * requested. This method will create a new instance of the extension. This instance will be returned for all subsequent
     * calls to this method with the same extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionClass
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if this configuration is no longer configurable and an extension of the specified type has not already
     *             been installed
     * @see #extensions()
     */
    protected <T extends Extension> T use(Class<T> extensionClass) {
        return container.useExtension(extensionClass);
    }
}
