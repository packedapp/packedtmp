package app.packed.bean;

import java.util.function.BiConsumer;

import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.inject.Factory;
import packed.internal.bean.OldBeanDriver.OtherBeanDriver;
import packed.internal.bean.PackedBeanDriverBinder;
import packed.internal.container.ContainerSetup;

/**
 * A bean extension support class.
 * 
 * Like other extension support classes this class is mainly used developers of extensions and not application developers.
 */
@ExtensionMember(BeanExtension.class)
// Maybe just BeanSupport, EntryPointSupport, WebSupport
public final class BeanSupportOld extends ExtensionSupport {

    /** The bean extension we are wrapping. */
    private final BeanExtension extension;

    /**
     * @param beanExtension
     */
    BeanSupportOld(BeanExtension beanExtension) {
        extension = beanExtension;
    }

    public <T, C extends BeanConfiguration<T>> C add(Object driver, C configuration, Class<? extends T> implementation) {
        // configuration, must be unattached
        throw new UnsupportedOperationException();
    }

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

    // Det er lidt for at undgaa BeanDriver<T,C>...
    public <B extends BeanConfiguration<?>> B populateConfiguration(B beanConfiguration) {
        return beanConfiguration;
    }

    public <T, C extends BeanConfiguration<T>> C wire(OtherBeanDriver<T, C> binder, Class<? extends T> implementation) {
        PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;

        ContainerSetup container = extension.container;
        return BeanExtension.wire(b.bind(implementation), container, container.realm);
    }

    public <T, C extends BeanConfiguration<T>> C wire(OtherBeanDriver<T, C> binder, Factory<? extends T> implementation) {
        PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;
        ContainerSetup container = extension.container;
        return BeanExtension.wire(b.bind(implementation), container, container.realm);
    }

    // installs a child to the specified component.
    // 1. Specifie ComponentConfiguration must be in the same container
    // 2. Specifie ComponentConfiguration must have been installed by the same extension
//    public <T, C extends BeanConfiguration<T>> C wireChild(ComponentConfiguration parent, OtherBeanDriver<T, C> binder, Class<? extends T> implementation) {
//        PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;
//        ContainerSetup container = extension.container;
//        return BeanExtension.wire(b.bind(implementation), container, container.realm);
//
//    }

    public <T, C extends BeanConfiguration<T>> C wireInstance(OtherBeanDriver<T, C> binder, T instance) {
        PackedBeanDriverBinder<T, C> b = (PackedBeanDriverBinder<T, C>) binder;
        ContainerSetup container = extension.container;
        return BeanExtension.wire(b.bindInstance(instance), container, container.realm);

    }
}