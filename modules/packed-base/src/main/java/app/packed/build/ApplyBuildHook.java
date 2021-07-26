package app.packed.build;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.hooks.MethodHook;

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
// Hvordan relatere den til ContainerDriver????

// ContainerDriver er der altid kun 1 af. En BuildProcessor kan der vaere flere af.

// Saa hvis vi f.eks. saetter typen (UNMANAGED) af en bean. Saa skal det vaere en driver
