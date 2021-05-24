package app.packed.application;

import app.packed.component.Assembly;
import app.packed.component.Wirelet;
import packed.internal.application.BaseMirror;

/**
 * The type of builds
 */
// Maaske ikke en enum, men en klasse
// Og saa rename til BuildMode
// Kunne vaere rart ogsaa at eksponere, isClosedWorld().
public enum BuildTarget {

    /**
     * An application image.
     * 
     * @see ApplicationDriver#newImage(Assembly, Wirelet...)
     */
    // Skal det bruges en gang, eller flere??? Er nok mere relevant end om det er et image...
    // MultiImage????
    IMAGE,

    /**
     * An application instance.
     * 
     * @see ApplicationDriver#compose(app.packed.component.Composer, java.util.function.Consumer, Wirelet...)
     * @see ApplicationDriver#launch(Assembly, Wirelet...)
     */
    INSTANCE, // LAUNCH

    /**
     * Builds a {@link BaseMirror mirror}.
     * 
     * @see ApplicationDriver#reflect(Assembly, Wirelet...)
     * @see BaseMirror#reflect(Assembly)
     */
    MIRROR,

    // Jeg tror efterhånden at man specificere det i assemblien...
    MULTI_IMAGE;
}
