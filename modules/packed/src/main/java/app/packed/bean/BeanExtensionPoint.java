package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import app.packed.base.TypeToken;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPointContext;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import packed.internal.bean.PackedBeanHandle;
import packed.internal.bean.PackedBeanHandleBuilder;
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
// Maybe just BeanSupport, EntryPointSupport, WebSupport

// 3 use cases
// installation af egne bean
// drivere for installation af assembly (application) beans
// drivere for installation af andre extensions beans

// Grunden til vi har drivere for de 2 sidste use cases og ikke bare install er
// hvis de bare vil installere helt almindelige beans. Saa kan man bruge BeanExtension/BeanSupport
// Direkte

public final class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

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
    private final ExtensionPointContext context;

    /**
     * Creates a new bean extension point
     * 
     * @param beanExtension
     *            the bean extension
     * @param context
     *            a context object about the user of this extension point
     */
    BeanExtensionPoint(BeanExtension beanExtension, ExtensionPointContext context) {
        this.container = beanExtension.container;
        this.context = context;
    }

    public <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        PackedBeanHandle<T> driver = PackedBeanHandleBuilder.ofClass(BeanKind.CONTAINER, container, BeanExtension.class, implementation).build();
        return new ExtensionBeanConfiguration<>(driver);
    }

    public <T> ExtensionBeanConfiguration<T> install(Factory<T> factory) {
        PackedBeanHandle<T> driver = PackedBeanHandleBuilder.ofFactory(BeanKind.CONTAINER, container, BeanExtension.class, factory).build();
        return new ExtensionBeanConfiguration<>(driver);
    }

    public <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        PackedBeanHandle<T> m = PackedBeanHandleBuilder.ofInstance(BeanKind.CONTAINER, container, BeanExtension.class, instance).build();
        return new ExtensionBeanConfiguration<>(m);
    }

    public BeanHandle.Builder<?> newBuilder(BeanKind kind) {
        return PackedBeanHandleBuilder.ofNone(kind, container, context.extensionType());
    }

    // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
    public <T> BeanHandle.Builder<T> newBuilderFromClass(BeanKind kind, Class<T> implementation) {
        return PackedBeanHandleBuilder.ofClass(kind, container, context.extensionType(), implementation);
    }

    public <T> BeanHandle.Builder<T> newBuilderFromFactory(BeanKind kind, Factory<T> factory) {
        return PackedBeanHandleBuilder.ofFactory(kind, container, context.extensionType(), factory);
    }

    public <T> BeanHandle.Builder<T> newBuilderFromInstance(BeanKind kind, T instance) {
        return PackedBeanHandleBuilder.ofInstance(kind, container, context.extensionType(), instance);
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
            return (Factory<T>) BeanExtensionPoint.defaultFactoryFor(cl);
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