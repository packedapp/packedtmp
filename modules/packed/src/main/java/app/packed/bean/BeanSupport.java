package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;

import app.packed.base.TypeToken;
import app.packed.component.Realm;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import packed.internal.bean.PackedBeanDriver;
import packed.internal.container.ContainerSetup;
import packed.internal.inject.factory.ReflectiveFactory.ExecutableFactory;
import packed.internal.invoke.typevariable.TypeVariableExtractor;
import packed.internal.util.BasePackageAccess;

/**
 * A bean extension support class.
 * 
 * Like other extension support classes this class is mainly used developers of extensions and not application
 * developers.
 */
@ExtensionMember(BeanExtension.class)
// Maybe just BeanSupport, EntryPointSupport, WebSupport
public final class BeanSupport extends ExtensionSupport {

    /**
     * A cache of factories used by {@link #of(TypeToken)}. This cache is only used by subclasses of TypeLiteral, never
     * literals that are manually constructed.
     */
    private static final ClassValue<ExecutableFactory<?>> TYPE_LITERAL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        protected ExecutableFactory<?> computeValue(Class<?> implementation) {
            Type t = TYPE_LITERAL_TV_EXTRACTOR.extract(implementation);
            TypeToken<?> tl = BasePackageAccess.base().toTypeLiteral(t);
            return new ExecutableFactory<>(tl, tl.rawType());
        }
    };

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_TV_EXTRACTOR = TypeVariableExtractor.of(TypeToken.class);

    /** The container we are installing beans into. */
    private final ContainerSetup container;

    // I think
    private final Class<? extends Extension<?>> extensionType;

    /**
     * @param beanExtension
     */
    /* package-private */ BeanSupport(BeanExtension beanExtension, Class<? extends Extension<?>> extensionType /* , c agent */) {
        this.container = beanExtension.container;
        this.extensionType = (extensionType);
    }

    public <T, P> void extensionPoint(ExtensionBeanConfiguration<T> consumerBean, BiConsumer<T, P> consumer, ContainerBeanConfiguration<P> providerBean) {

        // Skal vi checke at consumerBean bliver initialiseret foerend provider bean??? Ikke noedvendigt her...
        // Skal de vaere samme container??
        
        // Packed will call consumer(T, P) once provideBean has been initialized
    }

    public <T, P> void extensionPoint(ExtensionBeanConfiguration<T> consumerBean, BiConsumer<T, P> consumer, ExtensionBeanConfiguration<P> providerBean) {

        // Skal vi checke provideBean depends on consumerBean
        
        // framework will call
        // consumer(T, P) at initialization time
    }

    // ContainerBeanConfiguration... men den har provide.. Saa vi har en ExtensionBeanConfiguration
    public final <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        PackedBeanDriver<T> driver = PackedBeanDriver.ofClass(BeanKind.CONTAINER, container, Realm.extension(extensionType), implementation);
        return new ExtensionBeanConfiguration<>(driver);
    }

    public final <T> ExtensionBeanConfiguration<T> install(Factory<T> factory) {
        PackedBeanDriver<T> driver = PackedBeanDriver.ofFactory(BeanKind.CONTAINER, container, Realm.extension(extensionType), factory);
        return new ExtensionBeanConfiguration<>(driver);
    }

    public final <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        PackedBeanDriver<T> m = PackedBeanDriver.ofInstance(BeanKind.CONTAINER, container, Realm.extension(extensionType), instance);
        return new ExtensionBeanConfiguration<>(m);
    }

    public final BeanDriver<?> newDriver(BeanKind kind, Realm agent) {
        return PackedBeanDriver.ofNone(kind, container, agent);
    }

    public final <T> BeanDriver<T> newDriverFromClass(BeanKind kind, Realm agent, Class<T> implementation) {
        // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
        return PackedBeanDriver.ofClass(kind, container, agent, implementation);
    }

    public final <T> BeanDriver<T> newDriverFromFactory(BeanKind kind, Realm agent, Factory<T> factory) {
        return PackedBeanDriver.ofFactory(kind, container, agent, factory);
    }

    public final <T> BeanDriver<T> newDriverFromInstance(BeanKind kind, Realm agent, T instance) {
        return PackedBeanDriver.ofInstance(kind, container, agent, instance);
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
    // Flyt til BeanFactories
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> defaultFactoryFor(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return (Factory<T>) ExecutableFactory.DEFAULT_FACTORY.get(implementation);
    }

    /**
     * This method is equivalent to {@link #of(Class)} except taking a type literal.
     *
     * @param <T>
     *            the implementation type
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     */
    @SuppressWarnings("unchecked")
    // Hmm vi har jo ikke parameterized beans???
    public static <T> Factory<T> defaultFactoryFor(TypeToken<T> implementation) {
        // Can cache it with a Class[] array corresponding to type parameters...
        requireNonNull(implementation, "implementation is null");
        if (!implementation.isCanonicalized()) {
            // We cache factories for all "new TypeToken<>(){}"
            return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
        }
        Type t = implementation.type();
        if (t instanceof Class<?> cl) {
            return (Factory<T>) BeanSupport.defaultFactoryFor(cl);
        } else {
            ExecutableFactory<?> f = ExecutableFactory.DEFAULT_FACTORY.get(implementation.rawType());
            return new ExecutableFactory<>(f, implementation);
        }
    }
}
//
//public <T, C extends BeanConfiguration<T>> C add(Object driver, C configuration, Class<? extends T> implementation) {
//  // configuration, must be unattached
//  throw new UnsupportedOperationException();
//}

//
//public <B extends BeanConfiguration> B fullAccess(B beanConfiguration) {
//  // Tror vi require en annoteringen...
//  // Enten denne eller ogsaa skal vi require en annotation
//  return beanConfiguration;
//}