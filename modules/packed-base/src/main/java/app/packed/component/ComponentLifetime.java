package app.packed.component;

public enum ComponentLifetime {
    
    /** Lives and dies with the application. */
    APPLICATION,

    // Instantiated and deconstructed by an extension and some point
    EXTENSION_MANAGED,
    
    /** Once instances are initialized, Packed maintains no reference to them  */
    EXTENSION_UNMANAGED;
}
