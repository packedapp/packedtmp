package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.ExtensionPoint;
import app.packed.context.ContextUnavailableException;
import app.packed.context.Context;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.container.PackedExtensionPointContext;

/** An {@link ExtensionPoint extension point} class for {@link BeanExtension}. */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /** Creates a new bean extension point */
    BeanExtensionPoint() {}

    public <T> InstanceBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = newExtensionBean(BeanKind.CONTAINER, usageContext()).install(implementation);
        return new InstanceBeanConfiguration<>(handle);
    }

    /**
     * @param <T>
     *            the type of bean to install
     * @param op
     *            an operation responsible for creating an instance of the bean when the container is initialized
     * @return a configuration object representing the installed bean
     */
    public <T> InstanceBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<T> handle = newExtensionBean(BeanKind.CONTAINER, usageContext()).install(op);
        return new InstanceBeanConfiguration<>(handle);
    }

    public <T> ExtensionBeanConfiguration<T> installIfAbsent(Class<T> clazz) {
        return installIfAbsent(clazz, c -> {});
    }

    /**
     * <p>
     * The configuration might be di
     * 
     * @param <T>
     *            the type of bean to install
     * @param clazz
     * @param action
     * @return a bean configuration
     * @implNote the implementation may use to return different bean configuration instances for subsequent invocations.
     *           Even for action and the returned bean
     */
    public <T> ExtensionBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super InstanceBeanConfiguration<T>> action) {
        requireNonNull(action, "action is null");
        BeanHandle<T> handle = newExtensionBean(BeanKind.CONTAINER, usageContext()).installIfAbsent(clazz,
                h -> action.accept(new InstanceBeanConfiguration<>(h)));
        return new ExtensionBeanConfiguration<>(handle);
    }

    public <T> InstanceBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = newExtensionBean(BeanKind.CONTAINER, usageContext()).installInstance(instance);
        return new InstanceBeanConfiguration<>(handle);
    }

    /**
     * Installs a {@link BeanKind#STATIC static} bean.
     * 
     * @param beanClass
     *            the type of static bean to install
     * @return a configuration object representing the installed bean
     */
    public BeanConfiguration installStatic(Class<?> beanClass) {
        BeanHandle<?> handle = newExtensionBean(BeanKind.STATIC, usageContext()).install(beanClass);
        return new BeanConfiguration(handle);
    }

    /**
     * Creates a new installer for installing a bean for the application.
     * 
     * @param kind
     *            the kind of bean to installer
     * @return the installer
     */
    public BeanInstaller newApplicationBean(BeanKind kind) {
        return new PackedBeanInstaller(extension().extensionSetup, kind, (PackedExtensionPointContext) usageContext());
    }

    /**
     * Creates a new installer for installing a bean for another extension.
     * 
     * @param kind
     *            the kind of bean to installer
     * @return the installer
     */
    public BeanInstaller newExtensionBean(BeanKind kind, UsageContext forExtension) {
        requireNonNull(forExtension, "forExtension is null");
        return new PackedBeanInstaller(extension().extensionSetup, kind, (PackedExtensionPointContext) forExtension);
    }

    BeanHandle<?> unwrap(BeanConfiguration configuration) {
        // Can only call this on bean configurations that have been created by the extension itself.
        // But then could people just store it in a map...
        throw new UnsupportedOperationException();
    }

    /**
     * An installer for installing beans into a container.
     * <p>
     * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
     * are limited.
     * 
     * @see BeanExtensionPoint#newApplicationBean(BeanKind)
     * @see BeanExtensionPoint#newExtensionBean(BeanKind, app.packed.container.ExtensionPoint.UsageContext)
     */
    // Maybe put it back on handle. If we get OperationInstaller
    // Maybe Builder after all... Alle ved hvad en builder er
    public sealed static abstract class BeanInstaller permits PackedBeanInstaller {

        protected <T> BeanHandle<T> from(BeanSetup bs) {
            return new BeanHandle<>(bs);
        }

        /**
         * Installs the bean using the specified class as the bean source.
         * 
         * @param <T>
         *            the
         * @param beanClass
         * @return a bean handle representing the installed bean
         */
        public abstract <T> BeanHandle<T> install(Class<T> beanClass);

        public abstract <T> BeanHandle<T> install(Op<T> operation);

        public abstract <T> BeanHandle<T> installIfAbsent(Class<T> beanClass, Consumer<? super BeanHandle<T>> onInstall);

        public abstract <T> BeanHandle<T> installInstance(T instance);

        public abstract BeanHandle<Void> installWithoutSource();

        /**
         * An option that allows for a special bean introspector to be used when introspecting the bean for the extension.
         * Normally, the runtime would call {@link Extension#newBeanIntrospector} to obtain an introspector for the registering
         * extension.
         * 
         * @param introspector
         *            the introspector to use
         * @return the option
         * @see Extension#newBeanIntrospector
         */
        public abstract BeanInstaller introspectWith(BeanIntrospector introspector);

        // Hvad skal vi helt praecis goere her...
        // Vi bliver noedt til at vide hvilke kontekts der er...
        // Saa vi skal vel have OperationTemplates

        //// Hvad med @Get som laver en bean...
        //// Det er vel operationen der laver den...

        // No Lifetime, Container, Static, Functional, Static

        // Operational -> A bean that is instantiated and lives for the duration of an operation

        // MANYTONE -> Controlled

        public BeanInstaller lifetimeFromOperations() {
            return this;
        }

        public BeanInstaller lifetimes(OperationTemplate... confs) {
            return this;
        }

        /**
         * Allows multiple beans of the same type in a container.
         * <p>
         * By default, a container only allows a single bean of particular type if non-void.
         * 
         * @return this builder
         * @throws UnsupportedOperationException
         *             if bean kind is {@link BeanKind#FUNCTIONAL} or {@link BeanKind#STATIC}
         */
        public abstract BeanInstaller multi();

        public abstract BeanInstaller namePrefix(String prefix);

        BeanInstaller spawnNew() {
            // A bean that is created per operation.
            // Obvious manyton, but should we have own kind?
            // I actually think so because, because for now it always requires manyton

            // Some questions, do we support @Schedule? Or anything like it?
            // I don't think we need to set up the support for it by default. Only if used
            // So overhead is not needed

            // But I think those annotations that make sense are always "callback" extensions
            // From other threads
            // Single threaded vs multi-threaded
            // If we are single threaded it is obviously always only the request method
            // If we are multi threaded we create own little "world"
            // I think that is the difference, between the two

            // Maybe bean is always single threaded.
            // And container is always multi threaded

            throw new UnsupportedOperationException();
        }

        /**
         * Marks the bean as synthetic.
         * 
         * @return this installer
         */
        public abstract BeanInstaller synthetic();
    }

    /**
     *
     */
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    // ClassBindingHook, AnnotationBindingHook
    public @interface BindingHook {

        Class<?>[] allowedTypes() default {}; // ??

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();

        /**
         * Contexts that are required in order to use the binding class or annotation.
         * <p>
         * If this binding is attempted to be used without the context being available a {@link ContextUnavailableException}
         * will be thrown.
         * <p>
         * If this method returns multiple contexts they will <strong>all</strong> be required.
         * 
         * @return stuff
         */
        Class<? extends Context<?>>[] requiresContext() default {};

        // IDK about this...
        // Den virker jo kun for annotering..completesavings
        enum BindingOtherKind {
            // Peek -> Giver ikke mening vil jeg mene?
            ADAPT, CONVERT, DEFAULT, PEEK, PROVIDE, REPLACE, TRANSFORM
        }
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface ClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        // maybe allowAllAccess
        boolean allowFullPrivilegeAccess() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
        
        Class<? extends Context<?>>[] requiresContext() default {};
    }

    /**
     *
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface FieldHook {

        /** Whether or not the owning extension is allow to get the contents of the field. */
        boolean allowGet()

        default false;

        /** Whether or not the owning extension is allow to set the contents of the field. */
        boolean allowSet()

        default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
        
        Class<? extends Context<?>>[] requiresContext() default {};
    }

    /**
     * An annotation that indicates that the target is a method hook annotation.
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface MethodHook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as {@link BeanIntrospector.OnMethod#operationBuilder(ExtensionBeanConfiguration)} and... will fail with
         * {@link UnsupportedOperationException} unless the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanIntrospector.OnMethod#operationBuilder(ExtensionBeanConfiguration)
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        // invocationAccess
        boolean allowInvoke() default false; // allowIntercept...

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();

        // IDK, don't we just want to ignore it most of the time???
        // Nah maybe fail. People might think it does something
        boolean requiresVoidReturn() default false;
        
        Class<? extends Context<?>>[] requiresContext() default {};
    }
}

//// Ideen er at man fx kan have en handle.onInitialize(MyEBC, BeanHandle<Driver>, (b,p)->b.drivers[i]=p);

//// Ryger paa BeanHandle istedet for
//<B, P> void callbackOnInitialize(InstanceBeanConfiguration<B> extensionBean, BeanHandle<P> beanToInitialize, BiConsumer<? super B, ? super P> consumer) {
//    // ideen er at efter P er initialiseret saa kalder vi consumeren
//
//    // Smid den paa BeanHandle???
//    // <B> onInitialize(InstanceBeanConfiguration<B> extensionBean, BiConsumer<? super B, ? super P> consumer)
//    // <B> onInitialize(Class<B> extensionBeanClass, BiConsumer<? super B, ? super P> consumer)
//}
//
//// Same container I think0-=
//// Could we have it on initialize? Nahh, fungere vel egentligt kun med container beans
//<B, P> void callbackOnInitialize(InstanceBeanConfiguration<B> extensionBean, InstanceBeanConfiguration<P> beanToInitialize,
//        BiConsumer<? super B, ? super P> consumer) {
//    // Skal vi checke at consumerBean bliver initialiseret foerend provider bean???
//    // Ja det syntes jeg...
//    // Skal de vaere samme container??
//
//    // Packed will call consumer(T, P) once provideBean has been initialized
//    // Skal vi checke provideBean depends on consumerBean
//    // framework will call
//    // consumer(T, P) at initialization time
//
//}
// Idea was to return the same IBC always. But equals, hashcode is fixed on BeanConfiguration, so can use as key in maps
//class Tmp {
//  InstanceBeanConfiguration<T> conf;
//}
//Tmp tmp = new Tmp();
//BeanHandle<T> handle = newInstaller(BeanKind.CONTAINER, useSite()).InstallIfAbsent(clazz, h -> action.accept(tmp.conf = new InstanceBeanConfiguration<>(h)));
//return tmp.conf == null ? new InstanceBeanConfiguration<>(handle) : tmp.conf;
