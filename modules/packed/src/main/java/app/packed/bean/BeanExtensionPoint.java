package app.packed.bean;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import app.packed.base.TypeToken;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.ExtensionPoint;
import app.packed.operation.op.Op;
import internal.app.packed.bean.PackedBeanHandleInstaller;
import internal.app.packed.inject.factory.ReflectiveOp.ExecutableOp;

/**
 * An extension point class for {@link BeanExtension}.
 */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /** Creates a new bean extension point */
                                             /* package-private */ BeanExtensionPoint() {}

    /**
     * Create a new installer without a source.
     * 
     * @return the new installer
     */
    public BeanHandle.Installer<?> beanInstaller() {
        return PackedBeanHandleInstaller.ofNone(useSite(), extension().container);
    }

    /**
     * Create a new installer from a class source.
     * 
     * @param <T>
     *            type of bean
     * @param clazz
     *            the clazz
     * @return the new installer
     */
    public <T> BeanHandle.Installer<T> beanInstallerFromClass(Class<T> clazz) {
        return PackedBeanHandleInstaller.ofClass(useSite(), extension().container, clazz);
    }

    public <T> BeanHandle.Installer<T> beanInstallerFromInstance(T instance) {
        return PackedBeanHandleInstaller.ofInstance(useSite(), extension().container, instance);
    }

    public <T> BeanHandle.Installer<T> beanInstallerFromOp(Op<T> op) {
        return PackedBeanHandleInstaller.ofFactory(useSite(), extension().container, op);
    }

    public <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = PackedBeanHandleInstaller.ofClass(null, extension().container, implementation).forExtension(useSite()).kindSingleton().install();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> install(Op<T> factory) {
        BeanHandle<T> handle = PackedBeanHandleInstaller.ofFactory(null, extension().container, factory).forExtension(useSite()).kindSingleton().install();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = PackedBeanHandleInstaller.ofInstance(null, extension().container, instance).forExtension(useSite()).kindSingleton().install();
        return new ExtensionBeanConfiguration<>(handle);
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
    public static <T> Op<T> factoryOf(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return (Op<T>) ExecutableOp.DEFAULT_FACTORY.get(implementation);
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface ClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowAllAccess() default false;

        /** The extension the hook is a part of. */
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
         * Methods such as {@link BeanIntrospector$BeanMethod#operationBuilder(ExtensionBeanConfiguration)} and... will fail
         * with {@link UnsupportedOperationException} unless the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanIntrospector$BeanMethod#operationBuilder(ExtensionBeanConfiguration)
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        boolean allowInvoke() default false; // allowIntercept...

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RUNTIME)
    @Documented
    public @interface BindingHook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();
    }
}

class Sandbox {

    <T> ExtensionBeanConfiguration<T> installLazy(Class<T> implementation) {
        throw new UnsupportedOperationException();
    }
    
    <T> ExtensionBeanConfiguration<T> installLazy(Op<T> implementation) {
        throw new UnsupportedOperationException();
    }

    <T> ExtensionBeanConfiguration<T> installMany(Class<T> implementation) {
        throw new UnsupportedOperationException();
    }

    <T> ExtensionBeanConfiguration<T> installMany(Op<T> implementation) {
        throw new UnsupportedOperationException();
    }
    
    <T> ExtensionBeanConfiguration<T> installManyInstance(T instance) {
        throw new UnsupportedOperationException();
    }

    <T> ExtensionBeanConfiguration<T> installManyLazy(Class<T> implementation) {
        throw new UnsupportedOperationException();
    }

    <T> ExtensionBeanConfiguration<T> installManyLazy(Op<T> implementation) {
        throw new UnsupportedOperationException();
    }

    <T> ExtensionBeanConfiguration<T> installStatic(Class<T> implementation) {
        throw new UnsupportedOperationException();
    }
    
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
    public static <T> Op<T> defaultFactoryFor(TypeToken<T> implementation) {
        // Can cache it with a Class[] array corresponding to type parameters...
        requireNonNull(implementation, "implementation is null");
        if (!implementation.isCanonicalized()) {
            // We cache factories for all "new TypeToken<>(){}"
            // return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
            throw new UnsupportedOperationException();
        }
        Type t = implementation.type();
        if (t instanceof Class<?> cl) {
            return (Op<T>) BeanExtensionPoint.factoryOf(cl);
        } else {
            ExecutableOp<?> f = ExecutableOp.DEFAULT_FACTORY.get(implementation.rawType());
            return new ExecutableOp<>(f, implementation);
        }
    }
}

//
//public <B extends BeanConfiguration> B fullAccess(B beanConfiguration) {
//  // Tror vi require en annoteringen...
//  // Enten denne eller ogsaa skal vi require en annotation
//  return beanConfiguration;
//}