package app.packed.bean.hooks;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor.AbstractAnnotatedElementProcessor;

public /*sealed*/ abstract class BeanHookElement /*permits BeanClass,BeanField,BeanMethod,BeanMethodInterceptor*/ {

    /** Only visible to the various hook subclasses in this package. */
    BeanHookElement() {}

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

    //public static void $registerExtensionBean(Class<? extends ExtensionBean> cl) {}
}

/**
*
*/
// Does not exist anymore after vi are introducing InjectableVariable
// Der er et sted den kunne vaere interessant og det var til at sige
// Denne annotering kan kun bruges sammen med xyz..
// Men det kan man vel sige paa
abstract class BeanInjectableParameter {

    /** {@return the index of the parameter.} */
    public final int getIndex() {
        throw new UnsupportedOperationException();
    }

    /** {@return the underlying parameter} */
    public final Parameter getParameter() {
        throw new UnsupportedOperationException();
    }

    /** {@return the underlying parameter} */
    public final Executable getDeclaringExecutable() {
        return getParameter().getDeclaringExecutable();
    }

    public final Type parameterizedType() {
        return getParameter().getParameterizedType();
    }

    protected static final void $supportVarArgs() {
        throw new UnsupportedOperationException();
    }
}
