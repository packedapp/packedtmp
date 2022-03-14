package app.packed.bean;

public enum BeanKind {

    /** Lives and dies with the container it is installed into. */
    CONTAINER,

    /** Will always return void as the bean type. */
    FUNCTIONAL,
    
    /** Once an instance of the bean has been initialized, Packed (or the extension) maintains no reference to it. */
    UNMANAGED,

    // Instantiated by an extensions that
    // A single ideally operates within it
    OPERATION,

    // Instantiated and deconstructed by an extension and some point (For example,
    MANAGED,

//    /// Er det virkelig sin egen bean????
//    /// Eller gaelder der bare andre visibility regler for extensions...
//
//    /// Det det er, er jo at brugere er bedoevende ligeglad...
//    /// Jo faerre bean kinds jo bedre
//    
//    /// Export giver ikke mening for extensions...
//    /// Requirements giver ikke mening for extensions
//    /// Og saa alligevel maaske... Brugeren skal implementere service XYZ
//    EXTENSION,

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
