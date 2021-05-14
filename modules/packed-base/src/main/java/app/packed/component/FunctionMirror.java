package app.packed.component;

import java.lang.reflect.Type;

/**
 * A mirror of a function (component).
 */
public interface FunctionMirror extends ComponentMirror {

    Type functionType();
    Class<?> functionClass();
}
