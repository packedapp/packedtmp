package app.packed.application;

import internal.app.packed.application.ApplicationSetup;

// Input DAW (Application)Driver (Root)Assembly Wirelets

// Kan ikke have noget fra fx ContainerConfiguration

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

public sealed interface BuildTaskInfo permits ApplicationSetup {

    /** {@return the goal of the build task.} */
    BuildTaskGoal goal();

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

//    default LifetimeKind lifetimeKind() {
//        return LifetimeKind.MANAGED;
//    }

    // isHosted

    // managed by extension...

}
