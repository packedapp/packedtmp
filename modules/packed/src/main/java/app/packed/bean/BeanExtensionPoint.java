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
import app.packed.operation.Op;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.BeanInstaller;
import internal.app.packed.container.PackedExtensionPointContext;

/** An extension point class for {@link BeanExtension}. */
public class BeanExtensionPoint extends ExtensionPoint<BeanExtension> {

    /** Creates a new bean extension point */
    BeanExtensionPoint() {}

    public BeanHandle.Builder builder(BeanKind kind) {
        return new BeanInstaller(extension().extensionSetup, kind, (PackedExtensionPointContext) useSite());
    }

    public BeanHandle.Builder builder(BeanKind kind, UseSite forExtension) {
        requireNonNull(forExtension, "forExtension");
        return new BeanInstaller(extension().extensionSetup, kind, (PackedExtensionPointContext) forExtension);
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
