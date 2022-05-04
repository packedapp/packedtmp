package app.packed.bean;

import java.util.function.BiConsumer;

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
public final class ExtensionBeanConfiguration<E> extends InstanceBeanConfiguration<E> {

    /**
     * @param handle
     */
    public ExtensionBeanConfiguration(BeanHandle<E> handle) {
        super(handle);
    }

    public <P> void callbackOnInitialize(ContainerBeanConfiguration<P> beanToInitialize, BiConsumer<? super E, ? super P> consumer) {
        // Skal vi checke at consumerBean bliver initialiseret foerend provider bean??? Ikke noedvendigt her...
        // Skal de vaere samme container??

        // Packed will call consumer(T, P) once provideBean has been initialized
    }

    public <P> void callbackOnInitialize(ExtensionBeanConfiguration<P> beanToInitialize, BiConsumer<? super E, ? super P> consumer) {
        // Skal vi checke provideBean depends on consumerBean
        // framework will call
        // consumer(T, P) at initialization time
    }

    /** {@inheritDoc} */
    @Override
    public ExtensionBeanConfiguration<E> named(String name) {
        super.named(name);
        return this;
    }

//    // ebc.bind(OPPack.class)
//    // Det er jo faktisk et (syncthetic) factory vi skal binde...
//    // Gerne til BeanExtension?
//    public <K> ExtensionBeanConfiguration<T> provideDelayedInstance(Class<K> key, Supplier<@Nullable K> supplier) {
//        return provideDelayedInstance(Key.of(key), supplier);
//    }
//
//    public <K> ExtensionBeanConfiguration<T> provideDelayedInstance(Key<K> key, Supplier<@Nullable K> supplier) {
//        // Ideen er at vi binder lige foer vi laver wiringen.
//        throw new UnsupportedOperationException();
//    }
}
