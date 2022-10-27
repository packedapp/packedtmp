package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.container.Extension;
import app.packed.container.ExtensionPoint;
import app.packed.lifetime.LifetimeConf;
import app.packed.operation.Op;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.container.PackedExtensionPointContext;

/** An {@link ExtensionPoint extension point} class for {@link BeanExtension}. */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /** Creates a new bean extension point */
    BeanExtensionPoint() {}


    /**
     * An builder that can used by extensions to install new beans.
     * <p>
     * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
     * are limited.
     * 
     * @see BeanExtensionPoint#builder(BeanKind)
     * @see BeanExtensionPoint#builder(BeanKind, app.packed.container.ExtensionPoint.UseSite)
     */
    public sealed static abstract class BeanInstaller permits PackedBeanInstaller {

        /**
         * Installs the bean using the specified class as the bean source.
         * 
         * @param <T>
         *            the
         * @param beanClass
         * @return a bean handle representing the installed bean
         */
        public abstract <T> BeanHandle<T> build(Class<T> beanClass);

        public abstract <T> BeanHandle<T> build(Op<T> operation);

        public abstract <T> BeanHandle<T> buildFromInstance(T instance);

        public abstract BeanHandle<Void> buildSourceless();

        protected <T> BeanHandle<T> from(BeanSetup bs) {
            return new BeanHandle<>(bs);
        }

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

        public BeanInstaller lifetimes(LifetimeConf... confs) {
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
        public abstract BeanInstaller multiInstall();

        public abstract BeanInstaller namePrefix(String prefix);

        public abstract BeanInstaller onlyInstallIfAbsent(Consumer<? super BeanHandle<?>> onInstall);

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
    
    public BeanInstaller builder(BeanKind kind) {
        return new PackedBeanInstaller(extension().extensionSetup, kind, (PackedExtensionPointContext) useSite());
    }

    public BeanInstaller builder(BeanKind kind, UseSite forExtension) {
        requireNonNull(forExtension, "forExtension");
        return new PackedBeanInstaller(extension().extensionSetup, kind, (PackedExtensionPointContext) forExtension);
    }

    <B, P> void callbackOnInitialize(InstanceBeanConfiguration<B> extensionBean, BeanHandle<P> beanToInitialize, BiConsumer<? super B, ? super P> consumer) {

    }

    // Same container I think0-=
    // Could we have it on initialize? Nahh, fungere vel egentligt kun med container beans
    <B, P> void callbackOnInitialize(InstanceBeanConfiguration<B> extensionBean, InstanceBeanConfiguration<P> beanToInitialize,
            BiConsumer<? super B, ? super P> consumer) {
        // Skal vi checke at consumerBean bliver initialiseret foerend provider bean???
        // Ja det syntes jeg...
        // Skal de vaere samme container??

        // Packed will call consumer(T, P) once provideBean has been initialized
        // Skal vi checke provideBean depends on consumerBean
        // framework will call
        // consumer(T, P) at initialization time

    }

    public <T> InstanceBeanConfiguration<T> install(Class<T> implementation) {
        BeanHandle<T> handle = builder(BeanKind.CONTAINER, useSite()).build(implementation);
        return new ProvideableBeanConfiguration<>(handle);
    }

    public <T> InstanceBeanConfiguration<T> install(Op<T> op) {
        BeanHandle<T> handle = builder(BeanKind.CONTAINER, useSite()).build(op);
        return new ProvideableBeanConfiguration<>(handle);
    }

    // should not call anything on the returned bean
    public <T> InstanceBeanConfiguration<T> installIfAbsent(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public <T> InstanceBeanConfiguration<T> installIfAbsent(Class<T> clazz, Consumer<? super InstanceBeanConfiguration<T>> action) {
        throw new UnsupportedOperationException();
    }

    public <T> InstanceBeanConfiguration<T> installInstance(T instance) {
        BeanHandle<T> handle = builder(BeanKind.CONTAINER, useSite()).buildFromInstance(instance);
        return new ProvideableBeanConfiguration<>(handle);
    }

    /**
     *
     */
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface BindingHook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface ClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowAllAccess() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface FieldHook {

        /** Whether or not the owning extension is allow to get the contents of the field. */
        boolean allowGet() default false;

        /** Whether or not the owning extension is allow to set the contents of the field. */
        boolean allowSet() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    /**
     *
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
        boolean allowInvoke() default false; // allowIntercept...

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }
}
