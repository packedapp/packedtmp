package app.packed.bean;

import java.util.function.BiConsumer;

import app.packed.component.UserOrExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.inject.Factory;
import app.packed.inject.LookupFactory;
import packed.internal.bean.PackedBeanCustomizer;
import packed.internal.container.ContainerSetup;

/**
 * A bean extension support class.
 * 
 * Like other extension support classes this class is mainly used developers of extensions and not application
 * developers.
 */
@ExtensionMember(BeanExtension.class)
// Maybe just BeanSupport, EntryPointSupport, WebSupport
public final class BeanSupport extends ExtensionSupport {

    /** The container we will add beans into. */
    private final ContainerSetup container;

    // I think
    private final Class<? extends Extension<?>> extensionType;

    /**
     * @param beanExtension
     */
    /* package-private */ BeanSupport(BeanExtension beanExtension, Class<? extends Extension<?>> extensionType /* , UserOrExtension agent */) {
        this.container = beanExtension.container;
        this.extensionType = (extensionType);
    }
//
//    public <T, C extends BeanConfiguration<T>> C add(Object driver, C configuration, Class<? extends T> implementation) {
//        // configuration, must be unattached
//        throw new UnsupportedOperationException();
//    }

    public <T, P> void extensionPoint(ExtensionBeanConfiguration<T> myBean, BiConsumer<T, P> consumer, ContainerBeanConfiguration<P> provider) {

        // framework will call
        // consumer(T, P) at initialization time
    }

    public <T, P> void extensionPoint(ExtensionBeanConfiguration<T> myBean, BiConsumer<T, P> consumer, ExtensionBeanConfiguration<P> provider) {

        // framework will call
        // consumer(T, P) at initialization time
    }

    public <B extends BeanConfiguration> B fullAccess(B beanConfiguration) {
        // Enten denne eller ogsaa skal vi require en annotation
        return beanConfiguration;
    }

    // Kan ikke hedde install, hvis vi en dag beslutter vi godt vil have almindelige beans
    public final <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        PackedBeanCustomizer<T> m = PackedBeanCustomizer.ofFactory(container, UserOrExtension.extension(extensionType), LookupFactory.of(implementation));
        m.extensionBean();
        return new ExtensionBeanConfiguration<>(m);
    }

    public final <T> ExtensionBeanConfiguration<T> install(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public final <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        PackedBeanCustomizer<T> m = PackedBeanCustomizer.ofInstance(container, UserOrExtension.extension(extensionType), instance);
        m.extensionBean();
        return new ExtensionBeanConfiguration<>(m);

    }

    // *********************** ***********************
    // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
    public final <T> BeanCustomizer<T> newCustomizer(UserOrExtension agent, Class<T> implementation) {
        return PackedBeanCustomizer.ofFactory(container, agent, LookupFactory.of(implementation));
    }

    public final <T> BeanCustomizer<T> newCustomizer(UserOrExtension agent, Factory<T> factory) {
        return PackedBeanCustomizer.ofFactory(container, agent, factory);
    }

    public final <T> BeanCustomizer<T> newCustomizerInstance(UserOrExtension agent, T instance) {
        return PackedBeanCustomizer.ofInstance(container, agent, instance);
    }

    public final <T> BeanCustomizer<T> newSyntheticCustomizer(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }

    public final BeanCustomizer<Void> newFunctionalCustomizer(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }
}