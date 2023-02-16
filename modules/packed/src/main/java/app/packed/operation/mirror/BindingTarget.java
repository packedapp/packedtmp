/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.operation.mirror;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 *
 */
// Vi kan faa denne via OperationTarget, syntes jeg maaske er fint for nu
public sealed interface BindingTarget {

    /** Represents the binding of a {@link Field field}. */
    non-sealed interface OfField extends BindingTarget {

        /** {@return the field.} */
        Field field();
    }

    non-sealed interface OfMethodHandleParameter extends BindingTarget {}

    /**
     * Represents the binding of a {@link Parameter parameter} in a {@link Method method} or {@link Constructor
     * constructor}.
     */
    non-sealed interface OfParameter extends BindingTarget {

        /** {@return the parameters executable.} */
        Executable executable();

        /** {@return the index of the parameter.} */
        int index();

        /** {@return the parameter.} */
        Parameter parameter();
    }

    // Op1<@Foo fff, String> IDK kunne ogsaa vaere parameter...
    non-sealed interface OfTypeParameter extends BindingTarget {

    }
}
