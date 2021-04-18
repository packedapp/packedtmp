package app.packed.hooks.sandbox;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// InjectionHook

// Tror ikke vi kan definere mere end 1 InjectionHook per variable

@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
// InjectDynamic (Indy) Vi laver jo en bootstrap, Det er jo ligesom
public @interface InjectionHook {

    /** The hook's {@link Bootstrap} class. */
    Class<? extends InjectionHook.Bootstrap> bootstrap();

    /**
     * Any annotations that activates the method hook.
     * 
     * @return annotations that activates the method hook
     */
    Class<? extends Annotation>[] matchesAnnotation() default {};
    
    
    abstract class Bootstrap {
        
    }
}
