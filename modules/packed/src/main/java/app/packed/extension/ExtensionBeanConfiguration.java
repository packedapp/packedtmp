package app.packed.extension;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSupportOld;
import app.packed.inject.Factory;

/**
 * A special type of bean that can only be installed by an extension.
 * <p>
 * Lifetime / Lifecycle
 * <>
 * InjectionScope
 * 
 * @see BeanSupportOld#install(Class)
 * @see BeanSupportOld#install(Factory)
 * @see BeanSupportOld#installInstance(Object)
 */
// Taenker vi flytter den til .bean egentlig
public final class ExtensionBeanConfiguration<T> extends BeanConfiguration<T> {

    /**
     * @param handle
     */
    protected ExtensionBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind kind() {
        return BeanKind.EXTENSION;
    }
}
