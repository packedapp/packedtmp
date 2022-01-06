package packed.internal.bean.hooks.var2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/** An implementation of Var by wrapping a parameter.  */
public record ParameterVar(Parameter parameter) implements Var {

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return parameter.isAnnotationPresent(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return parameter.getAnnotationsByType(annotationClass);
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return parameter.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return parameter.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return parameter.getDeclaredAnnotations();
    }

    @Override
    public Class<?> getType() {
        return parameter.getType();
    }

    @Override
    public Type getParameterizedType() {
        return parameter.getParameterizedType();
    }
}
