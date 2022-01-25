package app.packed.extension;

import app.packed.bean.BeanMaker;
import app.packed.bean.BeanSupport;
import app.packed.bean.InstanceBeanConfiguration;
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
public final class ExtensionBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ExtensionBeanConfiguration(BeanMaker<T> handle) {
        super(handle);
    }
}
