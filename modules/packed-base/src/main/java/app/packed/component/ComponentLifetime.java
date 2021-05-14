package app.packed.component;

public enum ComponentLifetime {
    
    /** Lives and dies with the application. */
    APPLICATION,
    
    /** Once instances are initialized, Packed maintains no reference to them  */
    UNMANAGED,
    
}
