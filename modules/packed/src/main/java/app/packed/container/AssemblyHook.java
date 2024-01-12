package app.packed.container;

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
@Repeatable(AssemblyHook.All.class)
public @interface AssemblyHook {

    /**
     * Whether or not the hook applies only to the top container defined by the assembly. The default value is
     * {@code false}.
     *
     * @return whether or not the hook should be applied to all containers defined by the assembly or only the top level
     *         container
     *
     * @see ContainerConfiguration#isAssemblyRoot()
     */
    // We include all containers defined by the assembly per default. Because this is how BeanHooks would work.
    // And this is also how a would work

    // Hmm naar vi begynder fx at bruge namespaces saa virker den jo ikke super godt.
    // Taenker vi gerne vil kunne se apply on toplevel namespace only (per default)
    // Maaske skal vi have en annotering per transformer??
    boolean applyToTopAssemblyContainerOnly() default false;

    /**
     * {@return the transformer that should be applied to the container(s) defined by the assembly}
     * <p>
     * Implementations must be visible and instantiable to the framework. If using the module system this means that the
     * implementation should be accessible to the module of the framework and have a public constructor. Or the package in
     * which the implementation is located must be open to the framework.
     */
    Class<? extends ContainerTransformer>[] value();

    /** An annotation that allows for placing multiple {@link AssemblyHook} annotations on a single assembly. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of assembly hook declarations. */
        AssemblyHook[] value();
    }
    @interface AssemblyIs {

        Class<? extends Annotation>[] annotatedWithAny() default {};

        boolean rootInApplicationOnly() default false;

        // Can we have marker interfaces???
        Class<?>[] ofType() default {};

        String[] inModule() default {};
    }

}

//Include on Assembly??? as Assembly.Hook??? Naah, we have BeanHook as well I believe

// Just make a check in beforeBuild
//// The annotation can only be placed on the assembly that defines the application
//// Maybe just make
//boolean requiresApplicationAssembly() default false;

// After og before, Inner or Outer kan ogsaa bruge den med lifecycle
// boolean applyBeforeSubclasses() default true;

//Consumer<ContainerMirror>[] verifiers() default {};
