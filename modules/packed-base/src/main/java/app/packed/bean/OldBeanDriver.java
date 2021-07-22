package app.packed.bean;

import java.util.function.Function;

import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;

/**
 * A driver for creating bean components.
 * <p>
 * Except for the static methods on this interface. Bean drivers cannot be created directly. Instead binders are used
 */
public /* sealed */ interface OldBeanDriver<C extends BeanConfiguration> extends ComponentDriver<C> {

    /** {@inheritDoc} */
    @Override
    OldBeanDriver<C> with(Wirelet... wirelet);

    /**
     * A binder that can be used to bind class, factory or component class instance to create a bean driver.
     */
    /* sealed */ interface BeanDriver<T, C extends BeanConfiguration> {

        BeanType kind();

        BeanDriver<T, C> with(Wirelet... wirelet);
    }

    /* sealed */ interface Builder {

        <T, C extends BeanConfiguration> BeanDriver<T, C> build();

        // Specific super type

        Builder kind(BeanType kind);

        Builder namePrefix(Function<Class<?>, String> computeIt);

        Builder namePrefix(String prefix);

        Builder noInstances();

        // BeanConfigurationBinder<BeanConfiguration> buildBinder();
        Builder noReflection();

        Builder oneInstance();

        // Vi kan ikke rejecte extensions paa bean niveau...
        //// Man kan altid lave en anden extension som bruger den extension jo
        //// Saa det er kun paa container niveau vi kan forbyde extensions
        
        //// For instantiationOnly
        // reflectOnConstructorOnly();

        // reflectOn(Fields|Methods|Constructors)
        // look in declaring class
    }
}
