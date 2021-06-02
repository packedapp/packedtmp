package app.packed.hooks.v2;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.Extension;

/**
 * A hook
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnnotatedServiceHook {

    /** Bootstrap classes for this hook. */
    Class<? extends AnnotatedServiceHook.Bootstrap>[] bootstrap();

    /** Any extension this hook is part of. */
    Class<? extends Extension> extension();

    /** Any annotations that activates the method hook. */
    Class<? extends Annotation>[] matchesAnnotation() default {};

    static class Bootstrap {

        // Extension must have ConverterExtension as a dependency (if @Defauls() type!= parameter.type)
        void $enableDefaultValue() {}
    }
}
