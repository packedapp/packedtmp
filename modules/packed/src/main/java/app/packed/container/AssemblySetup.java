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
 * Can be applied on a subclass of
 * 
 * Assembly
 * 
 * Composer
 * 
 * Is ignored on any other classes
 */
// Rename to containerHook if we end up including composers, Which I think we will
// Skal vi kalde den andet end hook. Hvis MethodHook og friends er en abstract klasse...

// Hook er daarlig syntes fordi vi har de andre hooks
// AssembleAs

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(AssemblySetup.All.class)
public @interface AssemblySetup {

    //// Hvad med containers som extension's define????
    //// containers med realm=Appplication eller realm=SomeExtension
    /** {@return the processors that should be applied for every container the assembly defines.} */
    Class<? extends Processor>[] value();

    /** An annotation that allows for placing multiple assembly hook annotations on a single target. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of assembly hook declarations. */
        AssemblySetup[] value();
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
     */
    // Tror kun den kan bruges paa containers
    // Paa composeren har vi nogle callbacks hvor man kan saette ting op
    // Strengt tager kan vi have <T extends ContainerConfiguration> og saa fejler man bare med BuildException hvis det ikke
    // passer

    /// containerProcessor???
    /// Skal bare direct paa assemblien...
    // @Assembly.Hook(HookBuild)
    // Noget der ogsaa taler imod Assembly.Hook er at vi ikke kan have
    // Bean.Hook
    public interface Processor {

        /**
         * Invoked immediately after the runtime has called {@link Assembly#build()}. If {@link Assembly#build()} fails this
         * method will not be invoked.
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

        // on because it should be a notification thingy..
        // Or should we reserve on to Async

        // onSuccess???
        default void onCompleted(ContainerMirror mirror) {};

        //// Why AssemblyHook
        // Paa composers kan vi extende metoder der hjaelper os, og brugerne kan ikke laver "aspekter paa den maade"
        // Mht til ContainerConfiguration saa er det preBuild og postBuild ikke interessante fordi man kalder jo bare et eller
        //// andet
        // Saa vi vil maaske have et ContainerConfiguration.onComplete(Consumer<? super ContainerConfiguration)

        //// Not a listerner, because we might change things

        //// is invoked exactly once per hook instance. As the first method.
        // default void onBootstrap(Bootstrap bootstrap) {};

    }
}
// -> @AssemblyHook.Apply(xxxxx) nej ikke naar vi faar functioner
// i annoteringer

//// Hmm, tror ikke rigtig den fungere super godt...
//// Her er den faktisk en god grund til at der er forskel
//// paa link(containerAssembly) or link
// boolean includeInnercontainers();
