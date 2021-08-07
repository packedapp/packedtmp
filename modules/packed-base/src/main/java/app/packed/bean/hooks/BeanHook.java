package app.packed.bean.hooks;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor.AbstractAnnotatedElementProcessor;

public sealed abstract class BeanHook permits BeanMethodHook {

    BeanHook() {}

    public final AnnotatedElement actualAnnotations() {
        // Syntes maaske ikke det giver at ekspornere andet end meta-annoteringer
        throw new UnsupportedOperationException();
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

    abstract AbstractAnnotatedElementProcessor processor();
}
