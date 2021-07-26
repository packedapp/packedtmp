package app.packed.container;

import java.lang.invoke.MethodHandles;

import app.packed.build.BuildHook;

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

// Tror kun den kan bruges paa assemblies
// Paa composeren har vi nogle callbacks
// Strengt tager kan vi have <T extends ContainerConfiguration> og saa fejler man bare med BuildException hvis det ikke passer
public non-sealed interface AssemblyHook extends BuildHook {

    /**
     * Invoked immediately before {@link Assembly#build()}
     * 
     * @param configuration
     *            the configuration of the container
     */
    default void onPreBuild(ContainerConfiguration configuration) {};

    // I think this is reverse order or preBuild

    // Heh det er ogsaa en mulighed for at koere noget kode efter alle exceptions
    // Naeh.. For dette maa vaere inde alle exceptions skal koeres faerdige
    default void onPostBuild(ContainerConfiguration configuration) {};

    default void onCompleted(ContainerConfiguration configuration) {};
}
//// IDK, den er vel kun relevant for at validere deployed applications
//// Tror hellere man vil installere det via ApplicationDeployerMirror.
//// Eller evt. en Wirelet
//default void verifyChildren(ContainerMirror container) {};

//// Why AssemblyHook
// Paa composers kan vi extende metoder der hjaelper os, og brugerne kan ikke laver "aspekter paa den maade"
// Mht til ContainerConfiguration saa er det preBuild og postBuild ikke interessante fordi man kalder jo bare et eller andet
// Saa vi vil maaske have et ContainerConfiguration.onComplete(Consumer<? super ContainerConfiguration)

//// Not a listerner, because we might change things