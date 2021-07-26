package app.packed.bean;

public enum BeanLifetime {
    
    /** Lives and dies with the application. */
    APPLICATION,

    // Instantiated and deconstructed by an extension and some point
    MANAGED,
    
    /** Once instances are initialized, Packed maintains no reference to them  */
    UNMANAGED;
}
// Scoped vs unscoped