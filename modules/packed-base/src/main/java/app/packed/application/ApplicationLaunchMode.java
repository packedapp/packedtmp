package app.packed.application;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 * What kind of build.
 */
// Maaske ikke en enum, men en klasse
// Og saa rename til BuildMode
// Kunne vaere rart ogsaa at eksponere, isClosedWorld().
// isClosedWorld() ved du ikke for starten... Med mindre man eksplicit skal definere det...
// Hvilket jeg faktisk maaske er tilhaenger af...

// Immidiatly Launch, Delayed Launch, Multi-Launch
public enum ApplicationLaunchMode {

    /**
     * An application image.
     * 
     * @see ApplicationDriver#newImage(Assembly, Wirelet...)
     */
    // Skal det bruges en gang, eller flere??? Er nok mere relevant end om det er et image...
    // MultiImage????
    IMAGE,

    /**
     * Builds and instantiates an application.
     * 
     * @see ApplicationDriver#compose(app.packed.component.Composer, java.util.function.Consumer, Wirelet...)
     * @see ApplicationDriver#launch(Assembly, Wirelet...)
     */
    INSTANCE, // LAUNCH

    /**
     * A mirror of some kind, for example, an {@link ApplicationMirror}.
     *
     * @see ApplicationMirror#of(Assembly, Wirelet...)
     */
    MIRROR,

    // Jeg tror efterhånden at man specificere det i assemblien...
    MULTI_IMAGE;
}
// Was BuildTarget