package zandbox.internal.hooks2.repository;

import java.lang.annotation.Annotation;

import app.packed.base.Nullable;
import zandbox.internal.hooks2.bootstrap.AbstractBootstrapModel;

public interface HookRepository {

    default HookRepository expandForMetaAnnotations(Class<?> annotatedType) {
        // Ideen er lidt at vi kigger paa klasse meta hook annoteringer...
        // Og saa returnere et nyt repository... der hvad ved de skal goere med de annoteringer...
        return this;
    }

    default void forClassAnnotation(Class<? extends Annotation> annotationType) {
        throw new UnsupportedOperationException();
    }

    default void forConstructorAnnotation(Class<? extends Annotation> annotationType) {
        throw new UnsupportedOperationException();
    }

    // Her er nok taenkt at det er @Inject der styre at vi skal kigge paa typen...
    // For at se om det f.eks. er ServiceRegistry...@Inject ServiceRegistry
    default void forFieldRawType(Class<?> parameterType) {}

    default void forParameterAnnotation(Class<? extends Annotation> annotationType) {
        throw new UnsupportedOperationException();
    }

    default void forParameterRawType(Class<?> parameterType) {}

    /**
     * Tries to find a field hook model for the given annotation type.
     * 
     * @param annotationType
     *            the type of annotation
     * @return any hook that might exist for the given annotation type
     */
    @Nullable
    AbstractBootstrapModel lookupFieldAnnotation(Class<? extends Annotation> annotationType);

    @Nullable
    AbstractBootstrapModel lookupMethodAnnotation(Class<? extends Annotation> annotationType);
}
// injection i constructore/methods... bestemmer om vi scanner parametere...
// Det goer vi jo kun hvis vi siger giver mig en injectable MethodHandle...
// Kan jo ogsaa sige vi klare den selv
