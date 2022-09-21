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
 * Can be used on subclasses of {@link Assembly} or as a meta-annotation that can be applied to {@link Assembly}
 * subclass.
 * 
 * Assembly : Will be applied to all containers installed by the user
 * 
 * <p>
 * 
 * Composer
 * 
 * Extension
 * 
 * Is ignored on any other classes.
 */

// Vi inkludere alle containere defineret i en assembly.
// Fordi det er det vi ogsaa goer med beans hook. Det giver ingen mening ikke at goere det.
// Ligesom Lookup ogsaa gaelder alle containere

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(ContainerHook.All.class)
public @interface ContainerHook {

    /** {@return the processors that should be applied for every container the assembly defines.} */
    Class<? extends Processor>[] value();

    /**
     * @return
     */
    // applyToAllContainersInAssembly instead?
    boolean applyToRootOnly() default false; // maybe to true anyway

    /** An annotation that allows for placing multiple {@link ContainerHook} annotations on a single target. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of assembly hook declarations. */
        ContainerHook[] value();
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
// Saa vi vil maaske have et ContainerConfiguration.onComplete(Consumer<? super ContainerConfiguration)

//// Not a listerner, because we might change things

//// is invoked exactly once per hook instance. As the first method.
// default void onBootstrap(Bootstrap bootstrap) {};

/// Den tager alle containere defineret af en Assembly.
/// Men Ligesom Bean hooks gaelder alle beans...
/// Saa bliver ContainerHook noedt til at gaelde alle containers