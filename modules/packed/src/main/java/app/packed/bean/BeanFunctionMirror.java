package app.packed.bean;

import java.lang.reflect.Type;

/**
 * A mirror of a function (component).
 */
// FunctionalComponentMirror? Men saa boer de andre jo ogsaa hedde det
public /*non-sealed */ interface BeanFunctionMirror {

    Type functionType();
    
    Class<?> functionalInterface();
}
