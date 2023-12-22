package app.packed.container;

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

// We include all containers by default. Because that is also how lookup(Lookup) works (and bean hooks)
// Fordi det er det vi ogsaa goer med beans hook. Det giver ingen mening ikke at goere det.
// Ligesom Lookup ogsaa gaelder alle containere i den assembly
// Maybe we should force making a decision about it...

// Include on Assembly??? as Assembly.Hook??? Naah, we have BeanHook as well I believe

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(AssemblyHook.All.class)
public @interface AssemblyHook {

    /**
     * Whether or not the hook applies to all containers defined by the assembly. The default value is {@code true}.
     *
     * @return whether or not the hook should be applied to all containers defined by the assembly or only the top level
     *         container
     *
     * @see ContainerConfiguration#isAssemblyRoot()
     */
    // applyToTopContainerOnly default false()
    boolean applyToAllContainers() default true;

    /** {@return the interceptors that should be applied for every container the assembly defines.} */
    Class<? extends Interceptor>[] value();

    /** An annotation that allows for placing multiple {@link AssemblyHook} annotations on a single assembly. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of assembly hook declarations. */
        AssemblyHook[] value();
    }

    /**
     * An assembly hook is super cool
     *
     * <p>
     * For the methods on this interface taking a {@link ContainerConfiguration} the following applies:
     *
     * The realm of the container configuration will be this class. Any value specified to
     * {@link Assembly#lookup(MethodHandles.Lookup)} will be reset before next context or the actual build method
     *
     * <p>
     * Interceptor implementations must be visible and instantiable to the framework. If using the module system this means
     * that the implementation should be accessible to the module of the framework and have a public constructor. Or the
     * package in which the implementation is located must be open to the framework.
     */
    public interface Interceptor {

        /**
         * Invoked immediately after the runtime has called {@link Assembly#build()}.
         * <p>
         * For assemblies with multiple processors. The processors for this method will be invoked in the reverse order of
         * {@link #beforeBuild(ContainerConfiguration)}.
         * <p>
         * If {@link Assembly#build()} throws an exception this method will not be invoked.
         *
         * @param configuration
         *            the configuration of the container
         */
        // Should we take an Assembly???
        default void afterBuild(ContainerConfiguration configuration) {}

        /**
         * Invoked immediately before the runtime calls {@link Assembly#build()}.
         *
         * @param configuration
         *            the configuration of the container
         */
        // Should we take an Assembly???
        default void beforeBuild(ContainerConfiguration configuration) {}

        /**
         * When an application has finished building this method is called to check.
         * <p>
         *
         * on because it should be a notification thingy, or should we reserve on to Async
         *
         * onSuccess??? verify?
         *
         * @param mirror
         *            a mirror of the assembly to verify
         *
         * @see AssemblyMirror#containers()
         */
        // ? T
        // Do we take a ApplicationVerify thingy where we can register issues??? IDK
        default void verify(AssemblyMirror mirror) {}
    }
}

// Should applyTooA
// Maybe this is actually the processor that should know this...
// Nah bad if we will be able to specify consumers directly in annotation at some point


// Just make a check in beforeBuild
//// The annotation can only be placed on the assembly that defines the application
//// Maybe just make
//boolean requiresApplicationAssembly() default false;

// After og before, Inner or Outer kan ogsaa bruge den med lifecycle
// boolean applyBeforeSubclasses() default true;

//Consumer<ContainerMirror>[] verifiers() default {};

//// is invoked exactly once per hook instance. As the first method.
// default void onBootstrap(Bootstrap bootstrap) {};

/// Den tager alle containere defineret af en Assembly.
/// Men Ligesom Bean hooks gaelder alle beans...
/// Saa bliver ContainerHook noedt til at gaelde alle containers