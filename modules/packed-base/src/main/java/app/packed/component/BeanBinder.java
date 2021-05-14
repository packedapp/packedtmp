package app.packed.component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import packed.internal.component.PackedClassComponentBinder;

// A protected interface on Extension??? Hidden for ordinary users
/**
 * A binder that can be used to bind class, factory or component class instance to create a component driver.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public interface BeanBinder<T, C extends ComponentConfiguration> {

    // Container Lifetime, Eager singleton
    static BeanBinder<Object, BeanConfiguration> DEFAULT = PackedClassComponentBinder.APPLET_DRIVER;

    /**
     * @param instance
     * @return
     * @throws UnsupportedOperationException
     *             if class binding is not supported
     */
    ComponentDriver<C> bind(Class<? extends T> implementation);

    ComponentDriver<C> bind(Factory<? extends T> factory);

    /**
     * Binds the specified instance and returns a component driver that can be used for
     * {@link ContainerConfiguration#wire(ComponentDriver, Wirelet...)}
     * 
     * @param instance
     *            the instance to bind
     * @return a component driver that wraps this driver and the specified instance
     * @throws UnsupportedOperationException
     *             if instance binding is not supported
     * @throws IllegalArgumentException
     *             If the specified instance is class or factory instance. Or if the instance for other reasons is not valid
     *             for this driver
     */
    ComponentDriver<C> bindInstance(T instance);

    /** {@return a set containing all modes this driver supports. } */
    Set<? extends BeanMode> supportedModes();

    BeanBinder<T, C> with(Wirelet... wirelet);

    Optional<Class<? extends Extension>> extension();

    /**
     * Returns a driver that can be used to create stateless components.
     * 
     * @param <T>
     *            the type
     * @return a driver
     */
    private static PackedClassComponentBinder driver() {
        return PackedClassComponentBinder.STATELESS_DRIVER;
    }

    static ComponentDriver<BeanConfiguration> driverStateless(Class<?> implementation) {
        return driver().bind(implementation);
    }

    static <T> ComponentDriver<BeanConfiguration> functional(Class<?> implementation) {
        return driver().bind(implementation);
    }

    interface Builder {
        Builder noReflection();
        
        Builder noInstances();
        Builder oneInstance();
        

        Builder namePrefix(String prefix);
        Builder namePrefix(Function<Class<?>, String> computeIt);
        
        <T, C extends ComponentConfiguration> BeanBinder<T, C> build();
    }
}
