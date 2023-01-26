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
package app.packed.bindings.mirror;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 *
 */
public sealed interface BindingTarget {

    /** Represents an operation that gets, sets or updates a {@link Field field}. */
    non-sealed interface OfField extends BindingTarget {
        /** {@return the field.} */
        Field field();
    }
    
    non-sealed interface OfMethodHandleParameter extends BindingTarget {}

    //(@Foo fff)
    /** Represents an operation that gets, sets or updates a {@link Field field}. */
    non-sealed interface OfParameter extends BindingTarget {

        /** {@return the parameter.} */
        Parameter parameter();
    }
    
    // Op1<@Foo fff, String> IDK kunne ogsaa vaere parameter...
    non-sealed interface OfTypeParameter extends BindingTarget {
        
    }
}
