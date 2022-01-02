package app.packed.bean;

import java.util.function.BiConsumer;

import app.packed.component.UserOrExtension;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.inject.Factory;
import packed.internal.bean.PackedBeanHandle;
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

    /**
     * @param beanExtension
     */
    BeanSupport(BeanExtension beanExtension /* , UserOrExtension agent */) {
        this.container = beanExtension.container;
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
    public final <T> ExtensionBeanConfiguration<T> install(Class<?> implementation) {
        throw new UnsupportedOperationException();
    }

    public final <T> ExtensionBeanConfiguration<T> install(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public final <T> ExtensionBeanConfiguration<T> installInstance(Object instance) {
        throw new UnsupportedOperationException();
    }

    // *********************** ***********************
    // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
    public final <T> BeanHandle<T> register(UserOrExtension agent, Class<T> implementation) {
        return PackedBeanHandle.ofFactory(container, agent, implementation);
    }

    public final <T> BeanHandle<T> register(UserOrExtension agent, Factory<T> factory) {
        return PackedBeanHandle.ofFactory(container, agent, factory);
    }

    public final <T> BeanHandle<T> registerInstance(UserOrExtension agent, T instance) {
        return PackedBeanHandle.ofInstance(container, agent, instance);
    }

    public final <T> BeanHandle<T> registerSynthetic(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }
    
    public final BeanHandle<Void> registerFunctional(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }
}