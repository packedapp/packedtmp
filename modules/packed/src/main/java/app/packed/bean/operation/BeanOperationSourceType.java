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
package app.packed.bean.operation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 */
public enum BeanOperationSourceType {

    /** The operation is based on accessing a {@link Field} */
    FIELD,

    /** The operation is based on invoking a {@link Method} */
    METHOD,

    /** The operation is based on invoking a {@link Constructor} */
    CONSTRUCTOR,
    
    INSTANCE, // BeanInstance? Constant
    
    FUNCTION, // FunctionalInterface + FN??? Vil mene FN bliver unwrapped...
    
    CUSTOM; // Typically a MethodHandle
}

// Hvis vi har et factory der wrapper en metode er det saa method??? eller FN/factory
// Tror maaske vi skal unwrappe det

// Hvad er Factory.of(FooClass.class).bind(String.class, "doobar");
// Constructor eller Custom????