package app.packed.component;

import java.lang.reflect.Type;

/**
 * A mirror of a function (component).
 */
// FunctionalComponentMirror? Men saa boer de andre jo ogsaa hedde det
public interface FunctionMirror extends ComponentMirror {

    Type functionType();
    
    Class<?> functionalInterface();
}
