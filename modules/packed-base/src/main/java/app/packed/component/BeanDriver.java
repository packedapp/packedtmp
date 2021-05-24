package app.packed.component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import app.packed.component.BeanMirror.BeanMode;
import app.packed.container.BaseContainerConfiguration;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import packed.internal.component.bean.PackedBeanDriverBinder;

/**
 * A driver for creating specialized bean components.
 */
public interface BeanDriver<C extends BeanConfiguration> extends ComponentDriver<C> {

    /**
     * A binder that can be used to bind class, factory or component class instance to create a bean driver.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public interface Binder<T, C extends BeanConfiguration> {

        // Container Lifetime, Eager singleton
        static Binder<Object, BaseBeanConfiguration> DEFAULT = PackedBeanDriverBinder.APPLET_DRIVER;

        /**
         * @param instance
         * @return
         * @throws UnsupportedOperationException
         *             if class binding is not supported
         */
        BeanDriver<C> bind(Class<? extends T> implementation);

        BeanDriver<C> bind(Factory<? extends T> factory);

        /**
         * Binds the specified instance and returns a component driver that can be used for
         * {@link BaseContainerConfiguration#wire(ComponentDriver, Wirelet...)}
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
        BeanDriver<C> bindInstance(T instance);

        /** {@return a set containing all modes this driver supports. } */
        Set<? extends BeanMode> supportedModes();

        Binder<T, C> with(Wirelet... wirelet);

        Optional<Class<? extends Extension>> extension();

        /**
         * Returns a driver that can be used to create stateless components.
         * 
         * @param <T>
         *            the type
         * @return a driver
         */
        private static PackedBeanDriverBinder driver() {
            return PackedBeanDriverBinder.STATELESS_DRIVER;
        }

        static ComponentDriver<BaseBeanConfiguration> driverStateless(Class<?> implementation) {
            return driver().bind(implementation);
        }

        static <T> ComponentDriver<BaseBeanConfiguration> functional(Class<?> implementation) {
            return driver().bind(implementation);
        }

        interface Builder {
            Builder noReflection();
            
            Builder noInstances();
            Builder oneInstance();
            

            Builder namePrefix(String prefix);
            Builder namePrefix(Function<Class<?>, String> computeIt);
            
            <T, C extends BeanConfiguration> Binder<T, C> build();
        }
    }
    
    public interface Builder {
        //BeanConfigurationBinder<BeanConfiguration> buildBinder();
    }
}
