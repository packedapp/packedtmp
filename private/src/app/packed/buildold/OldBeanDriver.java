package app.packed.buildold;

import app.packed.bean.BeanConfiguration;

/**
 * A driver for creating bean components.
 * <p>
 * Except for the static methods on this interface. Bean drivers cannot be created directly. Instead binders are used
 */
public /* sealed */ interface OldBeanDriver<C extends BeanConfiguration<?>> {


    /**
     * A binder that can be used to bind class, factory or component class instance to create a bean driver.
     */
    /* sealed */ interface OtherBeanDriver<T, C extends BeanConfiguration<?>> {

    }

}
