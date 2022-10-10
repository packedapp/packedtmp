package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.bean.BeanHandle.LifetimeConf;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.ExtensionPoint;
import app.packed.operation.Op;
import internal.app.packed.bean.BeanProps;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.op.ReflectiveOp.ExecutableOp;

/** An extension point class for {@link BeanExtension}. */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /** Creates a new bean extension point */
    BeanExtensionPoint() {}

    public <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        ExtensionSetup es = usedBy(useSite());
        BeanHandle<T> handle = BeanProps.installClass(extension().extensionSetup, es.extensionRealm, es, BeanKind.CONTAINER, implementation);
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> install(Op<T> factory) {
        ExtensionSetup es = usedBy(useSite());
        BeanHandle<T> handle = BeanProps.installOp(extension().extensionSetup, es.extensionRealm, es, BeanKind.CONTAINER, factory);
        return new ExtensionBeanConfiguration<>(handle);
    }

    // should not call anything on the returned bean
    public <T> ExtensionBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super ExtensionBeanConfiguration<T>> action) {
        throw new UnsupportedOperationException();
    }

    public <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        ExtensionSetup es = usedBy(useSite());
        BeanHandle<T> handle = BeanProps.installInstance(extension().extensionSetup, es.extensionRealm, es, instance);
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> BeanHandle<T> newContainerBean(Class<T> clazz, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installClass(be.extensionSetup, be.container.realm, null, BeanKind.CONTAINER, clazz);
    }

    public <T> BeanHandle<T> newContainerBean(Op<T> op, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installOp(be.extensionSetup, be.container.realm, null, BeanKind.CONTAINER, op);
    }

    public <T> BeanHandle<T> newContainerBean(UseSite extension, Class<T> clazz, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installClass(extension().extensionSetup, es.extensionRealm, es, BeanKind.CONTAINER, clazz);
    }

    public <T> BeanHandle<T> newContainerBean(UseSite extension, Op<T> op, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installOp(extension().extensionSetup, es.extensionRealm, es, BeanKind.CONTAINER, op);
    }

    public <T> BeanHandle<T> newContainerBeanFromInstance(T instance, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installInstance(be.extensionSetup, be.container.realm, null, instance);
    }

    public <T> BeanHandle<T> newContainerBeanFromInstance(UseSite extension, T instance, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installInstance(extension().extensionSetup, es.extensionRealm, es, instance);
    }

    /**
     * Create a new installer without a source.
     * 
     * @return the new installer
     */
    public BeanHandle<?> newFunctionalBean(BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installFunctional(be.extensionSetup, be.container.realm, null);
    }

    public BeanHandle<?> newFunctionalBean(UseSite extension, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installFunctional(extension().extensionSetup, es.extensionRealm, es);
    }

    public <T> BeanHandle<T> newLazyBean(Class<T> clazz, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installClass(be.extensionSetup, be.container.realm, null, BeanKind.LAZY, clazz);
    }

    public <T> BeanHandle<T> newLazyBean(Op<T> op, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installOp(be.extensionSetup, be.container.realm, null, BeanKind.LAZY, op);
    }

    public <T> BeanHandle<T> newLazyBean(UseSite extension, Class<T> clazz, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installClass(extension().extensionSetup, es.extensionRealm, es, BeanKind.LAZY, clazz);
    }

    public <T> BeanHandle<T> newLazyBean(UseSite extension, Op<T> op, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installOp(extension().extensionSetup, es.extensionRealm, es, BeanKind.LAZY, op);
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
    public <T> BeanHandle<T> newManytonBean(Class<T> clazz, LifetimeConf lifetimes, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installClass(be.extensionSetup, be.container.realm, null, BeanKind.MANYTON, clazz);
    }

    public <T> BeanHandle<T> newManytonBean(Op<T> op, LifetimeConf lifetimes, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installOp(be.extensionSetup, be.container.realm, null, BeanKind.MANYTON, op);
    }

    public <T> BeanHandle<T> newManytonBean(UseSite extension, Class<T> clazz, LifetimeConf lifetimes, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installClass(extension().extensionSetup, es.extensionRealm, es, BeanKind.MANYTON, clazz);
    }

    public <T> BeanHandle<T> newManytonBean(UseSite extension, Op<T> op, LifetimeConf lifetimes, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installOp(extension().extensionSetup, es.extensionRealm, es, BeanKind.MANYTON, op);
    }

    public <T> BeanHandle<T> newStaticBean(Class<T> clazz, BeanHandle.Option... options) {
        BeanExtension be = extension();
        return BeanProps.installClass(be.extensionSetup, be.container.realm, null, BeanKind.STATIC, clazz);
    }

    public <T> BeanHandle<T> newStaticBean(UseSite extension, Class<T> clazz, BeanHandle.Option... options) {
        ExtensionSetup es = usedBy(extension);
        return BeanProps.installClass(extension().extensionSetup, es.extensionRealm, es, BeanKind.STATIC, clazz);
    }

    private ExtensionSetup usedBy(UseSite useSite) {
        return ((PackedExtensionPointContext) useSite).usedBy();
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
}

class Sandbox {

    // should not call anything on the returned bean
    public <T> ExtensionBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super ExtensionBeanConfiguration<T>> action) {
        throw new UnsupportedOperationException();
    }

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

    // The problem is when we start calling methods such as .provide()
    // It doesn't really work to call such methods more than once.
    // Or at least the logic to ignore subsequent calls would be a bit annoying

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
//    // Hmm vi har jo ikke parameterized beans???
//    // Ved ikke om vi skal droppe den...
//    public static <T> Op<T> defaultFactoryFor(TypeToken<T> implementation) {
//        // Can cache it with a Class[] array corresponding to type parameters...
//        requireNonNull(implementation, "implementation is null");
//        if (!implementation.isCanonicalized()) {
//            // We cache factories for all "new TypeToken<>(){}"
//            // return (Factory<T>) TYPE_LITERAL_CACHE.get(implementation.getClass());
//            throw new UnsupportedOperationException();
//        }
//        Type t = implementation.type();
//        if (t instanceof Class<?> cl) {
//            return (Op<T>) BeanExtensionPoint.factoryOf(cl);
//        } else {
//            return (Op<T>) ExecutableOp.DEFAULT_FACTORY.get(implementation.rawType());
//        }
//    }
}

//
//public <B extends BeanConfiguration> B fullAccess(B beanConfiguration) {
//  // Tror vi require en annoteringen...
//  // Enten denne eller ogsaa skal vi require en annotation
//  return beanConfiguration;
//}