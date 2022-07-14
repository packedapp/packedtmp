package app.packed.application;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifetime.LifetimeKind;
import internal.app.packed.application.PackedApplicationInfo;

// Input DAW (Application) Driver Assembly Wirelets

//// Wirelets boer kun vaere build wirelets. Fx hvis nu havde time to live.
// Saa ville den jo kunne be overskrevet paa runtime

// Generiske user Attributes<> Kan saettes via Wirelets
/**
 * An immutable descriptor of an application.
 */
// Or Descriptor


//// Hmmm, vi kan ikke svare paa

// Er der entry points?
// Forventes der et result (JobExtension)f
// Hvad er navnet

public sealed interface ApplicationBuildInfo permits PackedApplicationInfo {
    
    // string name() would be nice... but unfortunantely is mutable....)
    
//
//    boolean isMirror();
//    
//    default void checkHasRunnable() {
//        checkHasRunnable("The required operation requires a runtime");
//    }
//
//    void checkHasRunnable(String message);

    //
//    /** {@return the type of the root container.} */
//    Class<? extends Assembly> containerType();
//    // defaultLaunchMode() -> Lazy
//
//    // Ved ikke om vi skal have den her...
//    // Den er maaske bare noget vi kan holde internt...
//    boolean isStaticImage();
//
//    boolean isClosedWorld(); // isStaticImage
//    
//    boolean isRestartable();

    default LifetimeKind lifetimeKind() {
        return LifetimeKind.MANAGED;
    }

    // isHosted

    // managed by extension...

    /**
     * What kind of build.
     */
    // Maaske ikke en enum, men en klasse
    // Og saa rename til BuildMode
    // Kunne vaere rart ogsaa at eksponere, isClosedWorld().
    // isClosedWorld() ved du ikke for starten... Med mindre man eksplicit skal definere det...
    // Hvilket jeg faktisk maaske er tilhaenger af...
    // Helt sikkert, hvis vi har statisk java er det jo ogsaa en given.

    // En app er Image, En Sessions child app er ReusableImage
    // ApplicationBuildTarget
    // ApplicationBuildType

    // Altsaa det er jo primaert extensions der skal bruge det.
    // Tror aldrig man kommer til at switche paa den
    // Saa er maaske ikke super brugbart
    public enum ApplicationBuildKind {

        /**
         * An application image will build the application. But delay the actual launch of the application to a later point.
         * <p>
         * A typical use case is native images
         * 
         * @see ApplicationDriver#imageOf(Assembly, Wirelet...)
         */
        // De andre bygger jo saadan set ogsaa...
        // buildOnly? IDK syntes vi skal finde et andet navn
        BUILD,
        
        // Skal helst hedde det samme som App.reusable taenker jeg
        BUILD_MULTIPLE,

        /**
         * Build and instantiate an application.
         * 
         * @see ApplicationDriver#launch(Assembly, Wirelet...)
         */
        LAUNCH,

        /**
         * Build a mirror of some kind, for example, an {@link ApplicationMirror}.
         *
         * @see App#mirrorOf(Assembly, Wirelet...)
         */
        MIRROR,

        // I really think we should have verify as well...
        // Tror det er et vigtigt signal at sende ved at have en void metode
        // verify
        VERIFY;
    }
}
