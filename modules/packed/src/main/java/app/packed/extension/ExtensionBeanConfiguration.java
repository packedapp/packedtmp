package app.packed.extension;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSupport;
import app.packed.inject.Factory;

/**
 * A special type of beans that can only be installed by an extension.
 * 
 * @see BeanSupport#install(Class)
 * @see BeanSupport#install(Factory)
 * @see BeanSupport#installInstance(Object)
 */
public final class ExtensionBeanConfiguration<T> extends BeanConfiguration<T> {

    /** {@inheritDoc} */
    @Override
    public BeanKind kind() {
        return BeanKind.EXTENSION;
    }
}
