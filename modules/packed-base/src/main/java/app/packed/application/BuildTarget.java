package app.packed.application;

import app.packed.component.Assembly;
import app.packed.component.Wirelet;

/**
 * The type of builds
 */
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
    INSTANCE,
    
    /**
     * Builds a {@link BuildMirror mirror}. 
     * 
     * @see ApplicationDriver#mirror(Assembly, Wirelet...)
     */
    MIRROR,

    MULTI_IMAGE;
}
