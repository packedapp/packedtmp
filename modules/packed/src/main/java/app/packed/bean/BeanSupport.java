package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;

import app.packed.base.TypeToken;
import app.packed.component.Realm;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.ExtensionSupportContext;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import packed.internal.bean.PackedBeanHandle;
import packed.internal.container.ContainerSetup;
import packed.internal.inject.factory.ReflectiveFactory.ExecutableFactory;
import packed.internal.util.BasePackageAccess;
import packed.internal.util.typevariable.TypeVariableExtractor;

/**
 * A bean extension support class.
 * 
 * Like other extension support classes this class is mainly used developers of extensions and not application
 * developers.
 */
/**
 *
 */
@ExtensionMember(BeanExtension.class)
// Maybe just BeanSupport, EntryPointSupport, WebSupport

// 3 use cases
// installation af egne bean
// drivere for installation af assembly (application) beans
// drivere for installation af andre extensions beans

// Grunden til vi har drivere for de 2 sidste use cases og ikke bare install er
// hvis de bare vil installere helt almindelige beans. Saa kan man bruge BeanExtension/BeanSupport
// Direkte

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

    /** The extension support context. */
    private final ExtensionSupportContext context;

    /**
     * @param beanExtension
     */
    /* package-private */ BeanSupport(BeanExtension beanExtension, ExtensionSupportContext context) {
        this.container = beanExtension.container;
        this.context = context;
    }

    // interface ExtensionPoint <- skal vi have et marker interface???
    public <T, P> void extensionPoint(ExtensionBeanConfiguration<T> consumerBean, BiConsumer<T, P> consumer, ContainerBeanConfiguration<P> producerBean) {

        // Skal vi checke at consumerBean bliver initialiseret foerend provider bean??? Ikke noedvendigt her...
        // Skal de vaere samme container??

        // Packed will call consumer(T, P) once provideBean has been initialized
    }

    public <T, P> void extensionPoint(ExtensionBeanConfiguration<T> consumerBean, BiConsumer<T, P> consumer, ExtensionBeanConfiguration<P> producerBean) {

        // Skal vi checke provideBean depends on consumerBean

        // framework will call
        // consumer(T, P) at initialization time
    }

    // ContainerBeanConfiguration... men den har provide.. Saa vi har en ExtensionBeanConfiguration
    public <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        PackedBeanHandle<T> driver = PackedBeanHandle.ofClass(BeanKind.CONTAINER, container, BeanExtension.class, context.realm(), implementation).build();
        return new ExtensionBeanConfiguration<>(driver);
    }

    public <T> ExtensionBeanConfiguration<T> install(Factory<T> factory) {
        PackedBeanHandle<T> driver = PackedBeanHandle.ofFactory(BeanKind.CONTAINER, container, BeanExtension.class, context.realm(), factory).build();
        return new ExtensionBeanConfiguration<>(driver);
    }

    public <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        PackedBeanHandle<T> m = PackedBeanHandle.ofInstance(BeanKind.CONTAINER, container, BeanExtension.class, context.realm(), instance).build();
        return new ExtensionBeanConfiguration<>(m);
    }

    public BeanHandle.Builder<?> builder(BeanKind kind) {
        return PackedBeanHandle.ofNone(kind, container, context.extensionType(), Realm.application());
    }

    // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
    public <T> BeanHandle.Builder<T> newApplicationBeanFromClass(BeanKind kind, Class<T> implementation) {
        return PackedBeanHandle.ofClass(kind, container, context.extensionType(), Realm.application(), implementation);
    }

    public <T> BeanHandle.Builder<T> newApplicationBeanFromFactory(BeanKind kind, Factory<T> factory) {
        return PackedBeanHandle.ofFactory(kind, container, context.extensionType(), context.realm(), factory);
    }

    public <T> BeanHandle.Builder<T> newApplicationBeanFromInstance(BeanKind kind, Realm realm, T instance) {
        return PackedBeanHandle.ofInstance(kind, container, context.extensionType(), context.realm(), instance);
    }

    public BeanHandle.Builder<?> newExtensionBean(BeanKind kind, ExtensionSupportContext context) {
        return PackedBeanHandle.ofNone(kind, container, this.context.extensionType(), context.realm());
    }

    public <T> BeanHandle.Builder<T> newExtensionBeanFromClass(BeanKind kind, ExtensionSupportContext context, Class<T> implementation) {
        return PackedBeanHandle.ofClass(kind, container, this.context.extensionType(), Realm.application(), implementation);
    }

    public <T> BeanHandle.Builder<T> newExtensionBeanFromFactory(BeanKind kind, ExtensionSupportContext context, Factory<T> factory) {
        return PackedBeanHandle.ofFactory(kind, container, this.context.extensionType(), context.realm(), factory);
    }

    public <T> BeanHandle.Builder<T> newExtensionBeanFromInstance(BeanKind kind, ExtensionSupportContext context, Realm realm, T instance) {
        return PackedBeanHandle.ofInstance(kind, container, this.context.extensionType(), context.realm(), instance);
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
//public <B extends BeanConfiguration> B fullAccess(B beanConfiguration) {
//  // Tror vi require en annoteringen...
//  // Enten denne eller ogsaa skal vi require en annotation
//  return beanConfiguration;
//}