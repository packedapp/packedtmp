package app.packed.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;

/**
 * An annotation that can be places on an assembly.
 * 
 * Can be used on subclasses of {@link Assembly} or as a meta-annotation that can be applied to {@link Assembly}
 * subclass.
 */
// We include all containers by default. Because that is also how lookup(Lookup) works (and bean hooks)
// Fordi det er det vi ogsaa goer med beans hook. Det giver ingen mening ikke at goere det.
// Ligesom Lookup ogsaa gaelder alle containere i den assembly
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(AssemblyHook.All.class)
public @interface AssemblyHook {

    /**
     * Whether or not the hook applies to all containers defined by the assembly. The default value is {@code true}.
     * 
     * @return whether or not the hook should be applied to all containers defined by the assembly
     */
    boolean applyToAllContainers() default true;

    /** {@return the processors that should be applied for every container the assembly defines.} */
    Class<? extends Processor>[] value();

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
     */
    public interface Processor {

        /**
         * Invoked immediately after the runtime has called {@link Assembly#build()}.
         * <p>
         * If {@link Assembly#build()} throws an exception this method will not be invoked.
         * 
         * @param configuration
         *            the configuration of the container
         */
        default void afterBuild(ContainerConfiguration configuration) {};

        /**
         * Invoked immediately before the runtime calls {@link Assembly#build()}
         * 
         * @param configuration
         *            the configuration of the container
         */
        default void beforeBuild(ContainerConfiguration configuration) {};

        /**
         * on because it should be a notification thingy, or should we reserve on to Async
         * 
         * onSuccess??? verify?
         * 
         * @param mirror
         */
        default void onCompleted(ContainerMirror mirror) {};
    }
}

// After og before, Inner or Outer kan ogsaa bruge den med lifecycle
// boolean applyBeforeSubclasses() default true;

//Consumer<ContainerMirror>[] verifiers() default {};

//// is invoked exactly once per hook instance. As the first method.
// default void onBootstrap(Bootstrap bootstrap) {};

/// Den tager alle containere defineret af en Assembly.
/// Men Ligesom Bean hooks gaelder alle beans...
/// Saa bliver ContainerHook noedt til at gaelde alle containers