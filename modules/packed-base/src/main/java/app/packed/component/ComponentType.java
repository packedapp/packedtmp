package app.packed.component;

// Er application runtime bare en extension?????
public enum ComponentType {
    CONTAINER, BEAN, FUNCTION, APPLICATION_HOST, 
    
    // Super nice, for saa kan man jo ogsaa angive om man kan benytte annoteringen paa en extension....
    EXTENSION_RUNTIME;
}
