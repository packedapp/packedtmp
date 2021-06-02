package app.packed.component;

import java.util.Optional;
import java.util.function.Function;

import app.packed.container.BaseContainerConfiguration;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import packed.internal.component.bean.PackedBeanDriverBinder;

/**
 * A driver for creating bean components.
 * <p>
 * Except for the static methods on this interface. Bean drivers cannot be created directly. Instead binders are used
 */
public /* sealed */ interface BeanDriver<C extends BeanConfiguration> extends ComponentDriver<C> {

    /** {@return the type of bean this is a driver for.} */
    Class<?> beanType();

    /** {@return the kind of bean this driver produces.} */
    BeanKind kind();

    /** {@inheritDoc} */
    @Override
    BeanDriver<C> with(Wirelet... wirelet);

    static BeanDriver<BaseBeanConfiguration> ofSingleton(Class<?> implementation) {
        return PackedBeanDriverBinder.SINGLETON_BEAN_BINDER.bind(implementation);
    }

    static BeanDriver<BaseBeanConfiguration> ofSingleton(Factory<?> factory) {
        return PackedBeanDriverBinder.SINGLETON_BEAN_BINDER.bind(factory);
    }

    static BeanDriver<BaseBeanConfiguration> ofSingletonInstance(Object instance) {
        return PackedBeanDriverBinder.SINGLETON_BEAN_BINDER.bindInstance(instance);
    }

    static BeanDriver<BaseBeanConfiguration> ofStatic(Class<?> implementation) {
        return PackedBeanDriverBinder.STATIC_BEAN_BINDER.bind(implementation);
    }

    /**
     * A binder that can be used to bind class, factory or component class instance to create a bean driver.
     */
    public /* sealed */ interface Binder<T, C extends BeanConfiguration> {

        /**
         * @param implementation
         *            the implementation to bind to
         * @return a new (bound) bean driver
         * @throws UnsupportedOperationException
         *             if class binding is not supported
         */

        BeanDriver<C> bind(Class<? extends T> implementation);

        // Som regel er det C<T> vi returnere...
        /**
         * @param factory
         * @return
         * @throws UnsupportedOperationException
         *             if attempting to bind a factory to binder with static kind.
         */
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

        Optional<Class<? extends Extension>> extension();

        BeanKind kind();

        Binder<T, C> with(Wirelet... wirelet);
    }

    public /* sealed */ interface Builder {

        <T, C extends BeanConfiguration> Binder<T, C> build();

        // Specific super type

        Builder kind(BeanKind kind);

        Builder namePrefix(Function<Class<?>, String> computeIt);

        Builder namePrefix(String prefix);

        Builder noInstances();

        // BeanConfigurationBinder<BeanConfiguration> buildBinder();
        Builder noReflection();

        Builder oneInstance();

        // Vi kan ikke rejecte extensions paa bean niveau...
        //// Man kan altid lave en anden extension som bruger den extension jo
        //// Saa det er kun paa container niveau vi kan forbyde extensions
    }
}

class ZlassComponentDriverBuilder {

    //// For instantiationOnly
    // reflectOnConstructorOnly();

    // reflectOn(Fields|Methods|Constructors)
    // look in declaring class
}
