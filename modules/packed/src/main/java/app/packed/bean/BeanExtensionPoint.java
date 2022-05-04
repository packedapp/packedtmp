package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import app.packed.base.TypeToken;
import app.packed.container.ExtensionPoint;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import packed.internal.bean.PackedBeanHandleBuilder;
import packed.internal.inject.factory.ReflectiveFactory.ExecutableFactory;

/**
 * An extension point class for the {@link BeanExtension}.
 */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /**
     * Creates a new bean extension point
     * 
     * @param beanExtension
     *            the bean extension
     * @param context
     *            the use site of this extension point
     */
       /* package-private */ BeanExtensionPoint() {}

    public <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofClass(null, BeanKind.CONTAINER, extension().container, implementation).ownedBy(useSite()).build();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> install(Factory<T> factory) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofFactory(null, BeanKind.CONTAINER, extension().container, factory).ownedBy(useSite()).build();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = PackedBeanHandleBuilder.ofInstance(null, BeanKind.CONTAINER, extension().container, instance).ownedBy(useSite()).build();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public BeanHandle.Builder<?> newBuilder(BeanKind kind) {
        return PackedBeanHandleBuilder.ofNone(useSite(), kind, extension().container);
    }

    public <T> BeanHandle.Builder<T> newBuilderFromClass(BeanKind kind, Class<T> implementation) {
        return PackedBeanHandleBuilder.ofClass(useSite(), kind, extension().container, implementation);
    }

    public <T> BeanHandle.Builder<T> newBuilderFromFactory(BeanKind kind, Factory<T> factory) {
        return PackedBeanHandleBuilder.ofFactory(useSite(), kind, extension().container, factory);
    }

    public <T> BeanHandle.Builder<T> newBuilderFromInstance(BeanKind kind, T instance) {
        return PackedBeanHandleBuilder.ofInstance(useSite(), kind, extension().container, instance);
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

}

class Sandbox {

    // I don't think we will use it
//
//    /**
//     * A cache of factories used by {@link #of(TypeToken)}. This cache is only used by subclasses of TypeLiteral, never
//     * literals that are manually constructed.
//     */
//    private static final ClassValue<ExecutableFactory<?>> TYPE_LITERAL_CACHE = new ClassValue<>() {
//
//        /** A type variable extractor. */
//        private static final TypeVariableExtractor TYPE_LITERAL_TV_EXTRACTOR = TypeVariableExtractor.of(TypeToken.class);
//
//        /** {@inheritDoc} */
//        protected ExecutableFactory<?> computeValue(Class<?> implementation) {
//            Type t = TYPE_LITERAL_TV_EXTRACTOR.extract(implementation);
//            TypeToken<?> tl = BasePackageAccess.base().toTypeLiteral(t);
//            return new ExecutableFactory<>(tl, tl.rawType());
//        }
//    };

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
    // Ved ikke om vi skal droppe den...
    public static <T> Factory<T> defaultFactoryFor(TypeToken<T> implementation) {
        // Can cache it with a Class[] array corresponding to type parameters...
        requireNonNull(implementation, "implementation is null");
        if (!implementation.isCanonicalized()) {
            // We cache factories for all "new TypeToken<>(){}"
            // return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
            throw new UnsupportedOperationException();
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