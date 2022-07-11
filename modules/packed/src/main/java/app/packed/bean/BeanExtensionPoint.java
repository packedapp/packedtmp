package app.packed.bean;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import app.packed.base.TypeToken;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.ExtensionPoint;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import internal.app.packed.bean.PackedBeanHandleBuilder;
import internal.app.packed.inject.factory.ReflectiveFactory.ExecutableFactory;

/**
 * An extension point class for {@link BeanExtension}.
 */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /** Creates a new bean extension point */
                                             /* package-private */ BeanExtensionPoint() {}

    public BeanHandler.Builder<?> beanBuilder(BeanKind kind) {
        return PackedBeanHandleBuilder.ofNone(useSite(), kind, extension().container);
    }

    public <T> BeanHandler.Builder<T> beanBuilderFromClass(BeanKind kind, Class<T> implementation) {
        return PackedBeanHandleBuilder.ofClass(useSite(), kind, extension().container, implementation);
    }

    public <T> BeanHandler.Builder<T> beanBuilderFromFactory(BeanKind kind, Factory<T> factory) {
        return PackedBeanHandleBuilder.ofFactory(useSite(), kind, extension().container, factory);
    }

    public <T> BeanHandler.Builder<T> beanBuilderFromInstance(BeanKind kind, T instance) {
        return PackedBeanHandleBuilder.ofInstance(useSite(), kind, extension().container, instance);
    }

    public <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        PackedBeanHandleBuilder.ofClass(null, BeanKind.CONTAINER, extension().container, implementation);
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofClass(null, BeanKind.CONTAINER, extension().container, implementation).ownedBy(useSite()).build();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> install(Factory<T> factory) {
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofFactory(null, BeanKind.CONTAINER, extension().container, factory).ownedBy(useSite()).build();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        BeanHandler<T> handle = PackedBeanHandleBuilder.ofInstance(null, BeanKind.CONTAINER, extension().container, instance).ownedBy(useSite()).build();
        return new ExtensionBeanConfiguration<>(handle);
    }

    <T> ExtensionBeanConfiguration<T> installOnce(Class<T> implementation, Consumer<? super T> consumer) {
        // maaske hellere bare et installEBean felt i extensionen...
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
    // Flyt til BeanFactories
    // InjectableFactoryOf
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> factoryOf(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return (Factory<T>) ExecutableFactory.DEFAULT_FACTORY.get(implementation);
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface ClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowAllAccess() default false;

        /** The hook's {@link BeanField} class. */
        Class<? extends Extension<?>> extension();
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface FieldHook {

        /** Whether or not the owning extension is allow to get the contents of the field. */
        boolean allowGet() default false;

        /** Whether or not the owning extension is allow to set the contents of the field. */
        boolean allowSet() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface MethodHook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as {@link BeanMethod#operationBuilder(ExtensionBeanConfiguration)} and... will fail with
         * {@link UnsupportedOperationException} unless the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanMethod#operationBuilder(ExtensionBeanConfiguration)
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        boolean allowInvoke() default false; // allowIntercept...

        /** The hook's {@link BeanField} class. */
        Class<? extends Extension<?>> extension();
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
            return (Factory<T>) BeanExtensionPoint.factoryOf(cl);
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