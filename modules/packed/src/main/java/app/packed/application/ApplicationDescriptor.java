package app.packed.application;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import packed.internal.application.PackedApplicationDescriptor;

// Input DAW (Application) Driver Assembly Wirelets
//// Wirelets boer kun vaere build wirelets. Fx hvis nu havde time to live.
// Saa ville den jo kunne be overskrevet paa runtime

// Generiske user Attributes<> Kan saettes via Wirelets
/**
 * An immutable descriptor of an application.
 */
public sealed interface ApplicationDescriptor permits PackedApplicationDescriptor {
//
//    boolean isMirror();
//    
//    default void checkHasRunnable() {
//        checkHasRunnable("The required operation requires a runtime");
//    }
//
//    void checkHasRunnable(String message);
//
    /** {@return the type of the root container.} */
    Class<? extends Assembly > containerType();
//    // defaultLaunchMode() -> Lazy
//
//    // Ved ikke om vi skal have den her...
//    // Den er maaske bare noget vi kan holde internt...
//    boolean isStaticImage();
//
//    boolean isClosedWorld(); // isStaticImage
//    
//    boolean isRestartable();

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

    // En app er Image, En Sessions child app er ReusableImage
    // ApplicationBuildTarget
    // ApplicationBuildType

    // Altsaa det er jo primaert extensions der skal bruge det.
    // Tror aldrig man kommer til at switche paa den
    // Saa er maaske ikke super brugbart
    public enum ApplicationBuildType {

        /**
         * An application image will build the application. But delay the actual launch of the application to a later point.
         * <p>
         * A typical use case is native images
         * 
         * @see ApplicationDriver#imageOf(Assembly, Wirelet...)
         */
        IMAGE,

        /**
         * Build and instantiate an application.
         * 
         * @see ApplicationDriver#launch(Assembly, Wirelet...)
         */
        INSTANCE, // LAUNCH

        /**
         * Build a mirror of some kind, for example, an {@link ApplicationMirror}.
         *
         * @see ApplicationMirror#of(Assembly, Wirelet...)
         */
        MIRROR,

        REUSABLE_IMAGE;

        public boolean isImage() {
            return this == IMAGE || this == REUSABLE_IMAGE;
        }
    }
}
