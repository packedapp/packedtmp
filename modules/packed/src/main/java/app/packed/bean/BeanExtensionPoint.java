package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.ExtensionPoint;
import app.packed.operation.Op;
import internal.app.packed.bean.PackedBeanHandleInstaller;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.op.ReflectiveOp.ExecutableOp;

/** An extension point class for {@link BeanExtension}. */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /** Creates a new bean extension point */
                                             /* package-private */ BeanExtensionPoint() {}

    public <T> ExtensionBeanConfiguration<T> install(Class<T> implementation) {
        ExtensionSetup es = usedBy(useSite());
        BeanHandle<T> handle = PackedBeanHandleInstaller.ofClass(extension().extensionSetup, es.extensionRealm, es, implementation, true).kindSingleton()
                .install();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> install(Op<T> factory) {
        ExtensionSetup es = usedBy(useSite());
        BeanHandle<T> handle = PackedBeanHandleInstaller.ofFactory(extension().extensionSetup, es.extensionRealm, es, factory).kindSingleton().install();
        return new ExtensionBeanConfiguration<>(handle);
    }

    // should not call anything on the returned bean
    public <T> ExtensionBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super ExtensionBeanConfiguration<T>> action) {
        throw new UnsupportedOperationException();
    }

    public <T> ExtensionBeanConfiguration<T> installInstance(T instance) {
        ExtensionSetup es = usedBy(useSite());
        BeanHandle<T> handle = PackedBeanHandleInstaller.ofInstance(extension().extensionSetup, es.extensionRealm, es, instance).kindSingleton().install();
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> BeanHandle.Installer<T> newContainerBean(Class<T> clazz) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofClass(be.extensionSetup, be.container.realm, null, clazz, true);
    }

    public <T> BeanHandle.Installer<T> newContainerBean(Op<T> op) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofFactory(be.extensionSetup, be.container.realm, null, op);
    }

    public <T> BeanHandle.Installer<T> newContainerBean(UseSite extension, Class<T> clazz) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofClass(extension().extensionSetup, es.extensionRealm, es, clazz, true);
    }

    public <T> BeanHandle.Installer<T> newContainerBean(UseSite extension, Op<T> op) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofFactory(extension().extensionSetup, es.extensionRealm, es, op);
    }

    public <T> BeanHandle.Installer<T> newContainerBeanFromInstance(T instance) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofInstance(be.extensionSetup, be.container.realm, null, instance);
    }

    public <T> BeanHandle.Installer<T> newContainerBeanFromInstance(UseSite extension, T instance) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofInstance(extension().extensionSetup, es.extensionRealm, es, instance);
    }

    /**
     * Create a new installer without a source.
     * 
     * @return the new installer
     */
    public BeanHandle.Installer<?> newFunctionalBean() {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofNone(be.extensionSetup, be.container.realm, null);
    }

    public BeanHandle.Installer<?> newFunctionalBean(UseSite extension) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofNone(extension().extensionSetup, es.extensionRealm, es);
    }

    public <T> BeanHandle.Installer<T> newLazyBean(Class<T> clazz) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofClass(be.extensionSetup, be.container.realm, null, clazz, true);
    }

    public <T> BeanHandle.Installer<T> newLazyBean(Op<T> op) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofFactory(be.extensionSetup, be.container.realm, null, op);
    }

    public <T> BeanHandle.Installer<T> newLazyBean(UseSite extension, Class<T> clazz) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofClass(extension().extensionSetup, es.extensionRealm, es, clazz, true);
    }

    public <T> BeanHandle.Installer<T> newLazyBean(UseSite extension, Op<T> op) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofFactory(extension().extensionSetup, es.extensionRealm, es, op);
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
    public <T> BeanHandle.Installer<T> newManytonBean(Class<T> clazz, boolean instantiate) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofClass(be.extensionSetup, be.container.realm, null, clazz, true);
    }

    public <T> BeanHandle.Installer<T> newManytonBean(Op<T> op) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofFactory(be.extensionSetup, be.container.realm, null, op);
    }

    public <T> BeanHandle.Installer<T> newManytonBean(UseSite extension, Class<T> clazz) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofClass(extension().extensionSetup, es.extensionRealm, es, clazz, true);
    }

    public <T> BeanHandle.Installer<T> newManytonBean(UseSite extension, Op<T> op) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofFactory(extension().extensionSetup, es.extensionRealm, es, op);
    }

    public <T> BeanHandle.Installer<T> newStaticBean(Class<T> clazz) {
        BeanExtension be = extension();
        return PackedBeanHandleInstaller.ofClass(be.extensionSetup, be.container.realm, null, clazz, false);
    }

    public <T> BeanHandle.Installer<T> newStaticBean(UseSite extension, Class<T> clazz) {
        ExtensionSetup es = usedBy(extension);
        return PackedBeanHandleInstaller.ofClass(extension().extensionSetup, es.extensionRealm, es, clazz, false);
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