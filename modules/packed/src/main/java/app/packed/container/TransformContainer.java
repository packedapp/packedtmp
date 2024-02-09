package app.packed.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import app.packed.assembly.BaseAssembly;
import app.packed.assembly.TransformAssembly.AssemblyMatcher;

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
@Repeatable(TransformContainer.All.class)
//@BuildHook(BuildHookTarget.ASSEMBLY)
public @interface TransformContainer {

    AssemblyMatcher[] matchAssembly() default {};

    /**
     * {@return the transformer that should be applied to the container(s) defined by the assembly}
     * <p>
     * Implementations must be visible and instantiable to the framework. If using the module system this means that the
     * implementation should be accessible to the module of the framework and have a public constructor. Or the package in
     * which the implementation is located must be open to the framework.
     */
    Class<? extends ContainerTransformer>[] value();

    Class<? extends Consumer<ContainerMirror>>[] predicateClass() default{};

    /** An annotation that allows for placing multiple {@link AssemblyHook} annotations on a single assembly. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of assembly hook declarations. */
        TransformContainer[] value();
    }

    @interface ContainerMatcher {

        String ifContainerInPath()

        default "*"; // Regexp on container path
        // Would it be nice to be able to do it with beans as well??
        // For example, if I cannot modify the bean. So like a " full path
        // Nah, I think just specifying the class would be fine

        String[] taggedWith() default {};

        boolean rootInApplication() default false;

        /**
         * @return the container is only matched if it is the root container in the assembly
         */
        boolean rootInAssembly() default false;
    }
}

// Hvordan kan vi extract dette.
// Taenker vi kigger paa fields, og proever at matche, og proever at lave en
// Alternativ kan man selv tilfoeje det via en transformer??? IDK
@TransformContainer(ContainerTransformer.class)
@interface ZyHook {
    AssemblyMatcher[] matchAssembly() default {};
}

@TransformContainer(matchAssembly = @AssemblyMatcher(inModule = "foo.base"), value = ContainerTransformer.class)
@ZyHook(matchAssembly = @AssemblyMatcher(inModule = "foo.base"))
abstract class ZyAss extends BaseAssembly {

}

//// Hmm Maybe we need an assembly hook... IDK
//// Alternative have Class<Assembly> as default value
//Class<? extends Assembly>[] definedInAssemblyOfType() default {};

//Include on Assembly??? as Assembly.Hook??? Naah, we have BeanHook as well I believe

// Just make a check in beforeBuild
//// The annotation can only be placed on the assembly that defines the application
//// Maybe just make
//boolean requiresApplicationAssembly() default false;

// After og before, Inner or Outer kan ogsaa bruge den med lifecycle
// boolean applyBeforeSubclasses() default true;

//Consumer<ContainerMirror>[] verifiers() default {};
