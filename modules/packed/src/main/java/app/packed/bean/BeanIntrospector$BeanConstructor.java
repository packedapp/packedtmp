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
package app.packed.bean;

import java.lang.reflect.Constructor;

import app.packed.bean.BeanIntrospector.BeanElement;
import app.packed.operation.OperationType;
import app.packed.operation.invokesandbox.OperationHandle;

/**
 * This class represents a {@link Constructor} on a bean.
 * <p>
 * Unlike {@link BeanIntrospector$BeanField} and {@link BeanIntrospector$BeanMethod}. There are no way to define hooks on constructors. Instead they must
 * be defined on a bean driver or a bean class. Which determines how constructors are processed.
 */
// Do we need a BeanExecutable??? Not sure we have a use case
// Or maybe we just have BeanMethod (Problem with constructor() though)
public non-sealed interface BeanIntrospector$BeanConstructor extends BeanElement {

    /** {@return the underlying constructor.} */
    Constructor<?> constructor();

    /** {@return a factory type for this method.} */
    OperationType factoryType();

    /**
     * Returns the modifiers of the constructor.
     * 
     * @return the modifiers of the constructor
     * @see Constructor#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with
     *          {@link Constructor#getModifiers()}
     */
    int getModifiers();

    OperationHandle newOperation();
}
