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
package app.packed.util;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * A shared superclass for the common functionality of class variables (static {@link Field fields}), instance variables
 * (non-static {@link Field fields}) and {@link Parameter parameter} variables.
 */
public interface VariableDescriptor extends AnnotatedElement {

    /**
     * Returns the {@code Class} object representing the class or interface that declares the variable.
     *
     * @return the declaring class of the variable
     * @see Field#getDeclaringClass()
     * @see Executable#getDeclaringClass()
     */
    Class<?> getDeclaringClass();

    /**
     * Get the modifier flags for this the variable, as an integer. The {@code Modifier} class can be used to decode the
     * modifiers.
     *
     * @return The modifier flags for this variable
     * @see Parameter#getModifiers()
     * @see Field#getModifiers()
     */
    int getModifiers();

    /**
     * Returns the name of the variable.
     *
     * @return the name of the variable
     * @see Field#getName()
     * @see Parameter#getName()
     */
    String getName();

    /**
     * Returns a class that identifies the type of the variable.
     *
     * @return a class that identifies the type of the variable
     * @see Parameter#getType()
     * @see Field#getType()
     */
    Class<?> getType();

    /**
     * Returns a type literal that identifies the generic type of the variable.
     *
     * @return a type literal that identifies the generic type of the variable
     * @see Parameter#getParameterizedType()
     * @see Field#getGenericType()
     */
    TypeLiteral<?> getTypeLiteral();

    /**
     * Returns true if the variable has a name.
     * <p>
     * This is always true for field variables. For parameter variables it depends on {@link Parameter#isNamePresent()}.
     *
     * @return true if and only if the parameter has a name.
     * @see Parameter#isNamePresent()
     * @see Parameter#getName()
     * @see Field#getName()
     */
    boolean isNamePresent();

    /**
     * Returns whether or not the type of the variable is a primitive type.
     *
     * @return whether or not the type of the variable is a primitive type
     *
     * @see Class#isPrimitive()
     */
    boolean isPrimitiveType();
}
