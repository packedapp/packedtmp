package app.packed.bean;

// Maybe BeanKind instead
//// Eller ogsaa snakker vi 
public enum BeanKind {

    /** Lives and dies with the container it is installed into. */
    CONTAINER,

    // Instantiated by an extensions that
    // A single ideally operates within it
    OPERATION,

    // Instantiated and deconstructed by an extension and some point (For example,
    MANAGED,

    /** Once an instance of the bean has been initialized, Packed (or the extension) maintains no reference to it. */
    UNMANAGED,

    /** Will always return void as the bean type. */
    FUNCTIONAL,

    EXTENSION,
    
    /** Is never present in the component tree. Will typically return void as the bean type */
    SYNTHETIC,
}
// Er ikke sikker paa den her
// Container == Extension paa alt paanaer injection visibility

// Scoped vs unscoped

enum BeanLifetime {

    /** Lives and dies with the container it is installed into. */
    CONTAINER,

    // Instantiated by an extensions that
    // A single ideally operates within it
    OPERATION,

    // Instantiated and deconstructed by an extension and some point (For example, JPA entity)
    MANAGED,

    /** Once an instance of the bean has been initialized, Packed (or the extension) maintains no reference to it. */
    CONSTRUCTED,

    /** Constructed by other people. For example a bean that must verified at some point. */
    UNMANAGED;
}
