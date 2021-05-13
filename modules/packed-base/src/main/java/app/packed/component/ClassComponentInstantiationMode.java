package app.packed.component;

// Maaske har man bare none som default...
// Saa kan det vaere Optional..
// Nah, nok bedre med switch at altid have en mode
public enum ClassComponentInstantiationMode {

    // --------------- NONE ---------------
    NO_INSTANTIATION,

    // --------------- ONE ---------------
    ONE_LAZY_CONTAINER, // Created lazily in the container in which is it installed. And has the same maximum lifetime

    ONE_EAGER_CONTAINER, // Created together with the container in which it is installed. And has the same lifetime
    
    // ------------- MANY -------------

    // Er gemt bag en MethodHandle, står både for at lave en evt. instans
    // Men ogsaa for at slaa den ned igen...
    // Kalderen faar aldrig instancen. Eller vi kan vel godt extract ting fra instancen?
    MANY_INVOKEABLE,

    // FX Service protype Provide....
    // Marks the component as not-closable
    MANY_OPEN, // spawn, prototype, fireAndForge

    // Fully managed instances by extension
    MANY_OPEN_CLOSE; // Constructer MH, Destructor MH
}
// MANY_REACTIVE... Kind of like Invokable...
//// Maybe it is MANY_REQUEST
