package app.packed.container;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.bean.BeanExtensionPoint;
import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
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
// er operationer som tager en EBC hvortil der skal injectes

public class ExtensionBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ExtensionBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
        // Validate
    }

    public <V> void bindDelayed(Class<V> key, Supplier<V> supplier) {

    }

    public <V> ExtensionBeanConfiguration<T> bindDelayed(Key<V> key, Supplier<V> supplier) {
        // delayedInitiatedWith
        // Taenker vi har et filled array som er available when initiating the lifetime

        // Her skal vi ogsaa taenke ind at det skal vaere en application singleton vi injecter ind i.
        // Altsaa vi vil helst ikke initiere en Session extension bean med MHs hver gang
        
        return this;
    }

    public <P> void callbackOnInitialize(BeanHandle<P> beanToInitialize, BiConsumer<? super T, ? super P> consumer) {

    }

    // Same container I think
    public <P> void callbackOnInitialize(InstanceBeanConfiguration<P> beanToInitialize, BiConsumer<? super T, ? super P> consumer) {
        // Skal vi checke at consumerBean bliver initialiseret foerend provider bean???
        // Ja det syntes jeg...
        // Skal de vaere samme container??

        // Packed will call consumer(T, P) once provideBean has been initialized
        // Skal vi checke provideBean depends on consumerBean
        // framework will call
        // consumer(T, P) at initialization time

    }

    /** {@inheritDoc} */
    @Override
    public ExtensionBeanConfiguration<T> named(String name) {
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
