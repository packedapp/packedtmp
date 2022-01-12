package app.packed.bean;

import java.util.function.BiConsumer;

import app.packed.component.UserOrExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.inject.Factory;
import packed.internal.bean.PackedBeanMaker;
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

    /** The bean extension we are wrapping. */
    private final ContainerSetup container;

    // I think 
    private final Class<? extends Extension<?>> extensionType;

    /**
     * @param beanExtension
     */
    BeanSupport(BeanExtension beanExtension, Class<? extends Extension<?>> extensionType /* , UserOrExtension agent */) {
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

    public <B extends BeanConfiguration<?>> B fullAccess(B beanConfiguration) {
        // Enten denne eller ogsaa skal vi require en annotation
        return beanConfiguration;
    }

    // Kan ikke hedde install, hvis vi en dag beslutter vi godt vil have almindelige beans
    public final <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        PackedBeanMaker<T> m = PackedBeanMaker.ofFactory(container, UserOrExtension.extension(extensionType), implementation);
        m.extensionBean();
        return new ExtensionBeanConfiguration<>(m);
    }

    public final <T> ExtensionBeanConfiguration<T> install(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public final <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        PackedBeanMaker<T> m = PackedBeanMaker.ofInstance(container, UserOrExtension.extension(extensionType), instance);
        m.extensionBean();
        return new ExtensionBeanConfiguration<>(m);

    }

    // *********************** ***********************
    // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
    public final <T> BeanMaker<T> newMaker(UserOrExtension agent, Class<T> implementation) {
        return PackedBeanMaker.ofFactory(container, agent, implementation);
    }

    public final <T> BeanMaker<T> newMaker(UserOrExtension agent, Factory<T> factory) {
        return PackedBeanMaker.ofFactory(container, agent, factory);
    }

    public final <T> BeanMaker<T> newMakerInstance(UserOrExtension agent, T instance) {
        return PackedBeanMaker.ofInstance(container, agent, instance);
    }

    public final <T> BeanMaker<T> newSyntheticMaker(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }

    public final BeanMaker<Void> newFunctionalMaker(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }
}