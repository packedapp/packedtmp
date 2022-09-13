package app.packed.bean;

// This class doesn't really work. I'm not sure we can describe a bean by a single kind.
// For example, if a bean is not managed.. it should be managed. But it is not here
// Maybe once we clear up lifetime this will work better
@Deprecated
public enum BeanKind {

    /** Will always return void as the bean type. */
    FUNCTIONAL,

    /** A static bean is stateless bean that has a non-void bean class. */
    STATIC,

    /**
     * Lives and dies with the container it is installed into. Is eagerly created. Only a single bean of the specified type
     * may exists in the container. Think we need to check other bean types as well.
     * <p>
     * non-void
     * 
     */
    SINGLETON,

    MANYTON,

    /** Once an instance of the bean has been initialized, Packed (or the extension) maintains no reference to it. */
    UNMANAGED,

    // Instantiated by an extensions that
    // A single ideally operates within it
    MANAGED_OPERATION,

    // Instantiated and deconstructed by an extension and some point (For example,
    MANAGED_LIFETIME;

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

//    public boolean hasInstances() {
//        return this != FUNCTIONAL;
//    }
}
// Er ikke sikker paa den her
// Container == Extension paa alt paanaer injection visibility

// Scoped vs unscoped
//
//enum BeanLifetime {
//
//    /** Lives and dies with the container it is installed into. */
//    CONTAINER,
//
//    // Instantiated by an extensions that
//    // A single ideally operates within it
//    OPERATION,
//
//    // Instantiated and deconstructed by an extension and some point (For example, JPA entity)
//    MANAGED,
//
//    /** Once an instance of the bean has been initialized, Packed (or the extension) maintains no reference to it. */
//    CONSTRUCTED,
//
//    /** Constructed by other people. For example a bean that must verified at some point. */
//    UNMANAGED;
//}
