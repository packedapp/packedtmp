package app.packed.bean;

// Maybe BeanKind instead
public enum BeanKind {

    /** Lives and dies with the application. */
    APPLICATION,

    // Instantiated and deconstructed by an extension and some point
    MANAGED,

    /** Once an instance of the bean has been initialized, Packed (or the extension) maintains no reference to it. */
    UNMANAGED;
}
// Scoped vs unscoped