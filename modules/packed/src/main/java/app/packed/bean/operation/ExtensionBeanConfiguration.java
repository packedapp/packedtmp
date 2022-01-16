package app.packed.bean.operation;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMaker;
import app.packed.bean.BeanSupport;
import app.packed.inject.Factory;

/**
 * A special type of bean that can only be installed by an extension.
 * <p>
 * Lifetime / Lifecycle
 * <>
 * InjectionScope
 * 
 * @see BeanSupport#install(Class)
 * @see BeanSupport#install(Factory)
 * @see BeanSupport#installInstance(Object)
 */
// Taenker vi flytter den til .bean egentlig
// Har vi behov for T??? IDK
public final class ExtensionBeanConfiguration<T> extends BeanConfiguration<T> {

    /**
     * @param handle
     */
    public ExtensionBeanConfiguration(BeanMaker<T> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind kind() {
        return BeanKind.EXTENSION;
    }
}
