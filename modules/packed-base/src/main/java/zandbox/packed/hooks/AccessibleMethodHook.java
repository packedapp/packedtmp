package zandbox.packed.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.base.Nullable;
import app.packed.extension.Extension;
import app.packed.service.ServiceExtension;
import zandbox.internal.hooks2.bootstrap.AccessibleMethodBootstrapModel;
import zandbox.internal.hooks2.bootstrap.AccessibleMethodBootstrapModel.BootstrapContext;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor;
import zandbox.packed.hooks.AccessibleMethodHook2.BootstrapType;

/**
 * A hook triggered by an annotation that allows to invoke a single method.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface AccessibleMethodHook {

    /** The {@link Bootstrap} class for this hook. */
    Class<? extends AccessibleMethodHook.Bootstrap> bootstrapBean() default Bootstrap.class;

    /** The {@link Bootstrap} class for this hook. */
    Class<? extends AccessibleMethodHook.Bootstrap> bootstrapExtensor() default Bootstrap.class;

    /** The extension this hook is a part of. */
    Class<? extends Extension> extension();

    /** The annotation that triggers the hook. */
    Class<? extends Annotation> onAnnotation();

    /**
     * A bootstrap class that determines how the hook is processed.
     * <p>
     * If a field is annotated is such a way that there are multiple hooks activated at the same and athere are multiple
     * hooks that each have A single bootstrap Hvad goer vi med abstract klasser her??? Det er maaske ikke kun performance
     * at vi skal cache dem. Ellers kan vi ligesom ikke holder kontrakten om kun at aktivere det en gang...
     */
    abstract class Bootstrap {

        /**
         * A bootstrap object using by this class. Should only be read via {@link #context()}. Updated via
         * {@link AccessibleMethodBootstrapModel}.
         */
        private @Nullable BootstrapContext context;

        // Taenker vi har lov til at smide reflection exception???
        protected void bootstrap() {}

        /** {@return the bootstrap object} */
        private BootstrapContext context() {
            // Maybe do like Assembly with a doBootstrap method
            BootstrapContext b = context;
            if (b == null) {
                throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
            }
            return b;
        }

        /** Disables the hook, no further processing will be done. */
        public final void disable() {
            context().disable();
        }

        /** {@return the field we are bootstrapping.} */
        public final Method method() {
            return processor().expose(getClass().getModule());
        }

        /**
         * Returns an annotated element from the method that is being bootstrapped.
         * 
         * @see AnnotatedElement#getAnnotation(Class)
         */
        public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return processor().getAnnotation(annotationClass);
        }

        public final Annotation[] getAnnotations() {
            return processor().getAnnotations();
        }

        public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
            return processor().getAnnotationsByType(annotationClass);
        }

        /**
         * Returns true if an annotation for the specified type is <em>present</em> on the hooked field, else false.
         * 
         * @param annotationClass
         *            the Class object corresponding to the annotation type
         * @return true if an annotation for the specified annotation type is present on the hooked field, else false
         * 
         * @see Field#isAnnotationPresent(Class)
         */
        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return processor().isAnnotationPresent(annotationClass);
        }

        /**
         * Returns a method handle that gives read access to the underlying field as specified by
         * {@link Lookup#unreflectGetter(Field)}.
         * 
         * @return a method handle getter
         */
        public final MethodHandle methodHandle() {
            return processor().unreflect();
        }

        private ClassBootstrapProcessor.MethodProcessor processor() {
            return context().processor;
        }
    }
}

@interface AccessibleMethodHook2 {

    /** The {@link BeanMethodBootstrap} class for this hook. */
    Class<? extends AccessibleMethodHook.Bootstrap>[] bootstrap();

    /** The {@link BeanMethodBootstrap} class for this hook. */
    BootstrapType[] bootstrapTypes();

    /** The extension this hook is a part of. */
    Class<? extends Extension> extension();

    /** The annotation that triggers the hook. */
    Class<? extends Annotation> onAnnotation();
    
    enum BootstrapType {
        BEAN, APPLICATION, EXTENSOR;
    }
}
// Den er ogsaa grim...
@AccessibleMethodHook2(bootstrap = AccessibleMethodHook.Bootstrap.class, bootstrapTypes = BootstrapType.BEAN, onAnnotation = Usss.class, extension = ServiceExtension.class)
@interface Usss {
    
    
}