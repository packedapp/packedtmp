package app.packed.bundle.sandbox;

import java.lang.invoke.MethodHandles;

import app.packed.build.BuildHook;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.BundleMirror;

/**
 * An assembly hook is super cool
 * 
 * <p>
 * For the methods on this interface taking a {@link BundleConfiguration} the following applies:
 * 
 * The realm of the container configuration will be this class. Any value specified to
 * {@link Bundle#lookup(MethodHandles.Lookup)} will be reset before next context or the actual build method
 * 
 */
// Tror kun den kan bruges paa bundles
// Paa composeren har vi nogle callbacks hvor man kan saette ting op
// Strengt tager kan vi have <T extends ContainerConfiguration> og saa fejler man bare med BuildException hvis det ikke passer
public non-sealed interface BundleHook extends BuildHook {

    /**
     * Invoked immediately after the runtime calls {@link Bundle#build()}
     * 
     * @param configuration
     *            the configuration of the container
     */
    default void afterBuild(BundleConfiguration configuration) {};

    /**
     * Invoked immediately before the runtime calls {@link Bundle#build()}
     * 
     * @param configuration
     *            the configuration of the container
     */
    default void beforeBuild(BundleConfiguration configuration) {};

    // on because it should be a notification thingy..
    // Or should we reserve on to Async
    default void onCompleted(BundleMirror mirror) {};
}

//// Why AssemblyHook
// Paa composers kan vi extende metoder der hjaelper os, og brugerne kan ikke laver "aspekter paa den maade"
// Mht til ContainerConfiguration saa er det preBuild og postBuild ikke interessante fordi man kalder jo bare et eller andet
// Saa vi vil maaske have et ContainerConfiguration.onComplete(Consumer<? super ContainerConfiguration)

//// Not a listerner, because we might change things

//// is invoked exactly once per hook instance. As the first method.
//default void onBootstrap(Bootstrap bootstrap) {};

interface ZBootstrap {
    // Invoked exactly once

    // altsaa vi kunne jo godt banne extensions, og force enable her... Men i praksis har det jo ingen betydning

}