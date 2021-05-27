package zandbox.packed.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.inject.Inject;
import packed.internal.hooks.var2.Var;
import packed.internal.util.StackWalkerUtil;
import zandbox.internal.hooks2.bootstrap.AccessibleFieldBootstrapModel;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor;
import zandbox.internal.hooks2.bootstrap.InjectableVariableBootstrapModel;
import zandbox.internal.hooks2.bootstrap.InjectableVariableBootstrapModel.BootstrapContext;

/**
 * <p>
 * I {@link #onClass()} and the target is a field, the field must be annotated with {@link Inject}.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented

// was named InjectableVariableHook

// Primaer funktionalitet.
// Informational Type + annotationer
// Injection -> Constant/instance, MH (beskriv parameterene som vars), Factory, @HookProvide
public @interface InjectAnnotatedVariableHook {

    /** The {@link Bootstrap} class for this hook. */
    Class<? extends InjectAnnotatedVariableHook.Bootstrap> bootstrapBean();

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
         * {@link AccessibleFieldBootstrapModel}.
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

        public final Type getActualParameterizedType() {
            return processor().var.getParameterizedType();
        }

        /**
         * @return the actual type of the underlying variable
         * @see Field#getType()
         * @see Parameter#getType()
         * @see ParameterizedType#getRawType()
         */
        public final Class<?> getActualType() {
            return processor().var.getType();
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

        public final Type getParameterizedType() {
            return processor().va.getParameterizedType();
        }

        /**
         * Returns a {@code Class} object that identifies the the declared type for the variable.
         * 
         * @return the declared type of the variable
         * 
         * @see Field#getType()
         * @see Parameter#getType()
         */
        public final Class<?> getDeclaredType() {
            return processor().va.type;
        }

        Class<?> getType(TypeParser tp) {
            throw new UnsupportedOperationException();
        }

        // Injectable and Assign??? Hmm don't match I think
        public void injectConstant(Object value) {
            
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

        /** {@return whether or not a nullable annotation is present.} */
        public final boolean isNullable() {
            return processor().va.isNullable();
        }

        private ClassBootstrapProcessor.VariableProcessor processor() {
            return context().processor;
        }
        
        Var var() {
            return processor().var;
        }

        protected static void $disableWildcardFlatning() {
            // rename disable-> cancel
            // Then we can use disable to other things.

            // Nullable + Optional not supported...
        }

        // noOptionalOrNullable
        protected static void $noOptionalOrNullable() {
            // Nullable + Optional not supported...
        }

        // Den fungere ikke super godt hvis vi f.eks.
        // Vil traekke ud om vi en Provider...
        // Eller optional... // isProvider() IDK
        // Kunne tage det som en parameter til bootstrap...

        // noAnnotationProcessing()
        // noProcesisng (type+annotation)
        protected static void $noTypeProcessing() {
            // instead of calling $$supportGenericArrayTypes+ $$supportWildcardTypes + ....
        }

        protected static void $supportAnyTypes() {
            // instead of calling $$supportGenericArrayTypes+ $$supportWildcardTypes + ....
        }

        // Type readType(int modifiers)

        // kunne ogsaa vaere kompileret i en int/enum set
        protected static void $supportArrays() {}

        protected static void $supportGenericArrayTypes() {}

        protected static void $supportParameterizedTypes() {}

        protected static void $supportTypeVariables() {
            InjectableVariableBootstrapModel.Builder.staticInitialize(StackWalkerUtil.SW.getCallerClass(), l -> l.supportTypeVariables = true);
        }

        protected static void $supportWildcardTypes() {
            // https://github.com/google/guice/issues/1282
            // Kotlin issues with insertion of ?
            InjectableVariableBootstrapModel.Builder.staticInitialize(StackWalkerUtil.SW.getCallerClass(), l -> l.supportWildcardTypes = true);
        }

        // Jeg er bare ikke super vild med alle de metoder... IDK
        class TypeParser {}
        // Evt har vi en TypeParser som man kan lave statisk...
    }
}
