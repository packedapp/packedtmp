package app.packed.application;

/**
 * The type of builds
 */

// Maaske er den i virkeligheden mere interessant paa en application???
public enum BuildTarget {

    /** Analysis. */
    ANALYSIS,

    /** An application image. */
    IMAGE,

    /** An application instance. */
    INSTANCE;
}
