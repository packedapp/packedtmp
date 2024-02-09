package app.packed.assembly;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be places on an assembly.
 *
 * Can be used on subclasses of {@link Assembly} or as a meta-annotation that can be applied to {@link Assembly}
 * subclass.
 * <p>
 * If multiple assembly hook annotations are present on a single assembly class and/or assembly super classes. The order
 * of execution is determined {@link Class#getAnnotationsByType(Class)}.
 *
 * <p>
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(TransformAssembly.All.class)
//@BuildHook(BuildHookTarget.ASSEMBLY)
public @interface TransformAssembly {

    /**
     * {@return the transformer that should be applied to the container(s) defined by the assembly}
     * <p>
     * Implementations must be visible and instantiable to the framework. If using the module system this means that the
     * implementation should be accessible to the module of the framework and have a public constructor. Or the package in
     * which the implementation is located must be open to the framework.
     */
    Class<? extends AssemblyTransformer>[] value();

    /** An annotation that allows for placing multiple {@link AssemblyHook} annotations on a single assembly. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of assembly hook declarations. */
        TransformAssembly[] value();
    }

    /**
     * An annotation
     */
    // How does this work with Delegating Assemblies?
    @interface AssemblyMatcher {

        // Include Delegating???I think so
        Class<? extends Annotation>[] annotatedWithAny() default {};

        // I don't think we allow wildcards
        /**
         * @return
         * @see Module#getName()
         */
        String[] inModule() default {};

        // Can we have marker interfaces on assemblies??? I don't think so
        Class<?>[] ofType() default {};

        boolean applicationRootOnly() default false;
    }
}
