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

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import internal.app.packed.util.PackedVariable;
import internal.app.packed.util.types.TypeUtil;

/**
 * In Packed a variable (this interface) represents an annotated type of some kind This interface represents a variable of some kind, for example, a {@link Field}, Pa
 *
 * {@code AnnotatedVariable} represents a type and an annotated element. A variable is simple wrapper around a
 * {@link TypeToken} and an {@link AnnotatedElement}. A variable is typically constructed from a {@link Field},
 * {@link Parameter}, {@link TypeVariable} or the return type of a {@link Method}. But it can also be synthetically
 * constructed.
 */
// javadoc from AnnotatedTypeVariable

// Vis ci skal supportere capture. Bliver vi noedt til at have <T> og en abstract klasse...

// toKey(); <-- kunne bruges til fx wirelets
// asKey();
// Maybe just of(Variable v)

// Concrete class extending TypeToken????
// tror vi bliver noedt til at have specielle metoder for repeatable annotations
// Syntes vi navngiver den som .

/**
 *
 * @apiNote this interface retains the naming where possible from {@link Field}, {@link Parameter} and
 *          {@link TypeVariable}
 */
public sealed interface Variable permits PackedVariable {

    /** {@return a list of annotations on the variable. */
    AnnotationList annotations();

    /**
     * Returns the raw type (Class) of the variable.
     *
     * @return the raw type of the variable
     *
     * @see Field#getType()
     * @see Parameter#getType()
     * @see Method#getReturnType()
     * @see ParameterizedType#getRawType()
     */
    default Class<?> rawType() {
        return TypeUtil.rawTypeOf(type());
    }

    Type type();

    default boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return annotations().containsType(annotationType);
    }

    static Variable of(Class<?> clazz) {
        return PackedVariable.ofRaw(clazz);
    }

    /**
     * Returns a variable representing the annotated type of the specified field.
     * <p>
     * Annotations on the returned variable
     *
     * @param field
     *            the field to return a variable from
     * @return the variable
     */
    static Variable fromField(Field field) {
        return PackedVariable.of(field.getAnnotatedType());
    }

    /**
     * Returns a variable from the return type of the specified executable.
     *
     * @param executable
     *            the executable to return a variable from
     * @return the variable
     *
     * @see Executable#getAnnotatedReturnType()
     */
    static Variable fromExecutableReturnType(Executable executable) {
        return PackedVariable.of(executable.getAnnotatedReturnType());
    }

    /**
     * Returns a variable representing the specified parameter.
     *
     * @param parameter
     *            the parameter to return a variable from
     * @return the variable
     */
    static Variable fromParameter(Parameter parameter) {
        return PackedVariable.of(parameter.getAnnotatedType());
    }

//    // Do we really want to support type variables??? I don't think so
//    // I think we want to
//    // Just have a generic one that take type
//    static Variable ofTypeVariable(TypeVariable<?> typeVariable) {
//        return new PackedVariable(typeVariable, new PackedVariableType.OfTypeVariable(typeVariable));
//    }
}
