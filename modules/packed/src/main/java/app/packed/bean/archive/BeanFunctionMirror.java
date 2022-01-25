package app.packed.bean.archive;

import java.lang.reflect.Type;

/**
 * A mirror of a function (component).
 */
// FunctionalComponentMirror? Men saa boer de andre jo ogsaa hedde det
// Tror det bliver til en Operation...
/*non-sealed */ interface BeanFunctionMirror {

    Type functionType();
    
    Class<?> functionalInterface();
}
