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
package app.packed.introspection;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;

/**
 * A shared superclass for the common functionality of class variables (static {@link Field fields}), instance variables
 * (non-static {@link Field fields}), {@link TypeVariable type variables}, and {@link Parameter parameter} variables.
 * 
 * @apiNote In the future, if the Java language permits, {@link VariableDescriptor} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface VariableDescriptor extends AnnotatedElement {

    /**
     * Returns the type of element, is typically used for error messages.
     *
     * @return the type of element
     */
    String descriptorTypeName();

    /**
     * Returns the {@code Class} object representing the class or interface that declares the variable.
     *
     * @apiNote
     * @return the declaring class of the variable
     * @see Field#getDeclaringClass()
     * @see Executable#getDeclaringClass()
     * @apiNote this method is called getDeclaringClass instead of declaringClass to be compatible with
     *          {@link Member#getDeclaringClass()}
     */
    Class<?> getDeclaringClass();

    /**
     * Get the modifier flags for this the variable, as an integer. The {@code Modifier} class can be used to decode the
     * modifiers.
     *
     * @return The modifier flags for this variable
     * @see Parameter#getModifiers()
     * @see Field#getModifiers()
     * @apiNote this method is called getModifiers instead of modifiers to be compatible with {@link Member#getModifiers()}
     */
    int getModifiers();

    /**
     * Returns the name of the variable.
     *
     * @return the name of the variable
     * @see Field#getName()
     * @see Parameter#getName()
     * @apiNote this method is called getName instead of name to be compatible with {@link Member#getName()}
     */
    String getName();

    /**
     * Returns the parameterizedType of the variable
     *
     * @return the parameterizedType of the variable
     * @see Field#getGenericType()
     * @see Parameter#getParameterizedType()
     */
    Type getParameterizedType();

    /**
     * Returns a class that identifies the type of the variable.
     *
     * @return a class that identifies the type of the variable
     * @see Parameter#getType()
     * @see Field#getType()
     */
    // TODO rename to type
    Class<?> getType();

    /**
     * Returns a type literal that identifies the generic type of the variable.
     *
     * @return a type literal that identifies the generic type of the variable
     * @see Parameter#getParameterizedType()
     * @see Field#getGenericType()
     */
    // TODO rename typeLiteral
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
     * Returns whether or not a {@link Nullable} annotation is present on the variable.
     * 
     * @return true if a nullable annotation is present, otherwise false
     */
    default boolean isNullable() {
        return isAnnotationPresent(Nullable.class);
    }
}

// Vi har droppet index
///**
// * The index of the variable, used when creating {@link Dependency} instances.
// * <p>
// * If this variable is a field, this method returns {@code 0}.
// *
// * @return index of the variable.
// */
//// int index();
