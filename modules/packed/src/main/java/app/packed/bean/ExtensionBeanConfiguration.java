package app.packed.bean;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Factory;

/**
 * A special type of bean that can only be installed by an extension.
 * <p>
 * Lifetime / Lifecycle <> InjectionScope
 * 
 * @see BeanExtensionPoint#install(Class)
 * @see BeanExtensionPoint#install(Factory)
 * @see BeanExtensionPoint#installInstance(Object)
 */
// Har vi behov for T??? Egentlig ikke men inject/onState fra InstanceBean er rare.

// Eneste grund til vi stadig har den er god at angive nogle steder...
// fx BeanSupport#extensionPoint

// Maybe a generic NestedBean type???
public final class ExtensionBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ExtensionBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
        
    }

    // interface ExtensionPoint <- skal vi have et marker interface???
    // Det kan ogsaa vaere en metode paa EBC!!!
    // callbackWhenInitialized
    public <P> void extensionPoint(BiConsumer<T, P> consumer, ContainerBeanConfiguration<P> producerBean) {

        // Skal vi checke at consumerBean bliver initialiseret foerend provider bean??? Ikke noedvendigt her...
        // Skal de vaere samme container??

        // Packed will call consumer(T, P) once provideBean has been initialized
    }

    public <P> void extensionPoint(BiConsumer<T, P> consumer, ExtensionBeanConfiguration<P> producerBean) {

        // Skal vi checke provideBean depends on consumerBean

        // framework will call
        // consumer(T, P) at initialization time
    }

    public <P> ExtensionBeanConfiguration<T> extensionPoint(ContainerBeanConfiguration<P> producerBean, BiConsumer<T, P> consumer) {
        // consumer is called when producer bean has been initialized
        return this;
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
