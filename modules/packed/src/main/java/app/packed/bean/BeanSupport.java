package app.packed.bean;

import java.util.function.BiConsumer;

import app.packed.component.UserOrExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import app.packed.inject.ReflectiveFactory;
import packed.internal.bean.PackedBeanDriver;
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
        PackedBeanDriver<T> m = PackedBeanDriver.ofFactory(container, UserOrExtension.extension(extensionType), BeanSupport.of(implementation));
        m.extensionBean();
        return new ExtensionBeanConfiguration<>(m);
    }

    public final <T> ExtensionBeanConfiguration<T> install(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public final <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        PackedBeanDriver<T> m = PackedBeanDriver.ofInstance(container, UserOrExtension.extension(extensionType), instance);
        m.extensionBean();
        return new ExtensionBeanConfiguration<>(m);

    }

    // *********************** ***********************
    // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
    public final <T> BeanDriver<T> newDriver(UserOrExtension agent, Class<T> implementation) {
        return PackedBeanDriver.ofFactory(container, agent, BeanSupport.of(implementation));
    }

    public final <T> BeanDriver<T> newDriver(UserOrExtension agent, Factory<T> factory) {
        return PackedBeanDriver.ofFactory(container, agent, factory);
    }

    public final <T> BeanDriver<T> newDriverInstance(UserOrExtension agent, T instance) {
        return PackedBeanDriver.ofInstance(container, agent, instance);
    }

    public final <T> BeanDriver<T> newSyntheticDriver(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new bean driver for a functional bean.
     * <p>
     * Operations are added to functional beans via {@link BeanDriver#addFunction(Object)}.
     * 
     * @param agent
     * @return the new driver
     */
    public final BeanDriver<Void> newFunctionalDriver(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Tries to find a single static method or constructor on the specified class using the following rules:
     * <ul>
     * <li>If a single static method (non-static methods are ignored) annotated with {@link Inject} is present a factory
     * wrapping the method will be returned. If there are multiple static methods annotated with Inject this method will
     * fail with {@link IllegalStateException}.</li>
     * <li>If a single constructor annotated with {@link Inject} is present a factory wrapping the constructor will be
     * returned. If there are multiple constructors annotated with Inject this method will fail with
     * {@link IllegalStateException}.</li>
     * <li>If there is exactly one public constructor, a factory wrapping the constructor will be returned. If there are
     * multiple public constructors this method will fail with {@link IllegalStateException}.</li>
     * <li>If there is exactly one protected constructor, a factory wrapping the constructor will be returned. If there are
     * multiple protected constructors this method will fail with {@link IllegalStateException}.</li>
     * <li>If there is exactly one package-private constructor, a factory wrapping the constructor will be returned. If
     * there are multiple package-private constructors this method will fail with {@link IllegalStateException}.</li>
     * <li>If there is exactly one private constructor, a factory wrapping the constructor will be returned. Otherwise an
     * {@link IllegalStateException} is thrown.</li>
     * </ul>
     * <p>
     * 
     * @param <T>
     *            the implementation type
     * @param implementation
     *            the implementation type
     * @return a factory for the specified type
     */
    // Todo rename to make (or just of....) Nej, syntes maaske den er find med find()...
    // Rename of()... syntes det er fint den hedder of()... og saa er det en fejl situation
    // Eneste er vi generalt returnere en optional for find metoder...
    // Har droppet at kalde den find... Fordi find generelt returnere en Optional...
    // Lad os se hvad der sker med Map og generiks
    // InjectSupport.defaultInjectable()
    
    // If @Initialize -> rename to findInitializer
    // Hvis vi nogensinde laver en BeanFactory klasse... Saa hoere de jo til der.
    public static <T> /* ReflectionFactory<T> */ Factory<T> of(Class<T> implementation) {
       return ReflectiveFactory.of(implementation);
    }
}