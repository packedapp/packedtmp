package app.packed.bean.hooks.usage;

/**
 *
 */
public enum BeanType {

    /**
     * A static bean.
     * <p>
     * A bean that is never instantiated and has no state.
     */
    // Altsaa jeg ved ikke hvor meget det der state er embedded i alt...
    // Om vi kan snakke om beans der ikke har state...
    // Request er en ting... 
    // Men vi kan ikke faa injecte f.x. configuration
    // Men hvorfor ikke get(@ConfigFoo("sdfsf"), String req) <--
    STATIC, // static?

    EXTENSION_BEAN,
    
    /**
     * A singleton bean.
     * <p>
     * A single instance of the bean is created together with the application instance. It is coterminous with the
     * application instance itself.
     */
    BASE,

    /**
     * A lazy singleton bean.
     * <p>
     */
    LAZY_SINGLETON,

    /**
     * A prototype bean.
     * <p>
     * Is always created by an extension. Once initialized, the bean is no longer tracked by neither Packed or the
     * extension. And example, is a service prototype. Which is created when requested. But how, when and if the client
     * chooses to dispose of it is
     */
    PROTOTYPE_UNMANAGED,

    /**
     * A tracked bean.
     * <p>
     * Typical examples, are requests
     */
    TRACKED // Managed
}


//Maaske har man bare none som default...
//Saa kan det vaere Optional..
//Nah, nok bedre med switch at altid have en mode

// The key realization here is that MANY_OPEN_CLOSE vs INVOKABLE is an implementation detail
//enum BeanInstantiationMode {
//
// // --------------- NONE ---------------
// NO_INSTANTIATION,
//
// // --------------- ONE ---------------
// ONE_LAZY_CONTAINER, // Created lazily in the container in which is it installed. And has the same maximum lifetime
//
// ONE_EAGER_CONTAINER, // Created together with the container in which it is installed. And has the same lifetime
// 
// // ------------- MANY -------------
//
// // Er gemt bag en MethodHandle, står både for at lave en evt. instans
// // Men ogsaa for at slaa den ned igen...
// // Kalderen faar aldrig instancen. Eller vi kan vel godt extract ting fra instancen?
// MANY_INVOKEABLE,
//
// // FX Service protype Provide....
// // Marks the component as not-closable
// MANY_OPEN, // spawn, prototype, fireAndForge
//
// // Fully managed instances by extension
// MANY_OPEN_CLOSE; // Constructer MH, Destructor MH
//}
//MANY_REACTIVE... Kind of like Invokable...
////Maybe it is MANY_REQUEST
