package app.packed.extension;

import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanSupport;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.inject.Factory;

/**
 * A special type of bean that can only be installed by an extension.
 * <p>
 * Lifetime / Lifecycle <> InjectionScope
 * 
 * @see BeanSupport#install(Class)
 * @see BeanSupport#install(Factory)
 * @see BeanSupport#installInstance(Object)
 */
// Har vi behov for T??? Egentlig ikke men inject/onState fra InstanceBean er rare.

// Eneste grund til vi stadig har den er god at angive nogle steder...
// fx BeanSupport#extensionPoint

public final class ExtensionBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ExtensionBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    public <K> ExtensionBeanConfiguration<T> bindPreWiring(Class<K> key, Supplier<@Nullable K> supplier) {
        return bindPreWiring(Key.of(key), supplier);
    }

    public <K> ExtensionBeanConfiguration<T> bindPreWiring(Key<K> key, Supplier<@Nullable K> supplier) {
        // Ideen er at vi binder lige foer vi laver wiringen.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ExtensionBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }
}
