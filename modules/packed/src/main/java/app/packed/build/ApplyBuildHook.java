package app.packed.build;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be applied on a subclass of
 * 
 * Assembly
 * 
 * Composer
 * 
 * Is ignored on any other classes
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
// Er nok mere frisk paa at dele processoren
// Repeatable... Fordi super classes skal have lov at definere dem...
@Repeatable(ApplyBuildHook.All.class)
// prefix methods on Context with on????
public @interface ApplyBuildHook {

    Class<? extends BuildHook>[] value();
    
    /**
     * An annotation that allows for placing multiple {@linkplain MethodHook @MethodHook} annotations on a single target. Is
     * typically used to define meta annotations with multiple method hook annotations.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of {@linkplain MethodHook @MethodHook} declarations. */
        ApplyBuildHook[] value();
    }

}
