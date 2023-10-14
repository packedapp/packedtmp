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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import internal.app.packed.util.PackedAnnotationList;
import internal.app.packed.util.PackedVariable;
import internal.app.packed.util.types.TypeUtil;

/**
 * A variable (this interface) represents a {@link Type} and zero or more annotations.
 * <p>
 *
 *
 *  an annotated type of some kind This interface represents a variable of some kind, for example, a {@link Field}, Pa
 *
 * {@code AnnotatedVariable} represents a type and an annotated element. A variable is simple wrapper around a
 * {@link TypeToken} and an {@link AnnotatedElement}. A variable is typically constructed from a {@link Field},
 * {@link Parameter}, {@link TypeVariable} or the return type of a {@link Method}. But it can also be synthetically
 * constructed.
 */
// javadoc from AnnotatedTypeVariable

// Vis ci skal supportere capture. Bliver vi noedt til at have <T> og en abstract klasse...

/**
 *
 * @apiNote this interface retains the naming where possible from {@link Field}, {@link Parameter} and
 *          {@link TypeVariable}
 */
public sealed interface Variable permits PackedVariable {

    /** {@return a list of annotations on the variable. */
    AnnotationList annotations();

    /** {@return {@code true} if this variable has any annotations, otherwise false.} */
    boolean isAnnotated();

    default boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return annotations().isPresent(annotationType);
    }

    /**
     * Returns the raw type (Class) of this variable.
     *
     * @return the raw type of this variable
     */
    default Class<?> rawType() {
        return TypeUtil.rawTypeOf(type());
    }

    /** {@return the type of this variable.} */
    Type type();

    /**
     * Returns a variable representing the specified field.
     * <p>
     * The variable is constructed using {@link Field#getGenericType()} and {@link Field#getAnnotations()}.
     *
     * @param field
     *            the field to return a variable from
     * @return the variable
     */
    static Variable fromField(Field field) {
        return PackedVariable.of(field.getGenericType(), field.getAnnotations());
    }

    /**
     * Returns a variable representing the specified parameter.
     *
     * @param parameter
     *            the parameter to return a variable from
     * @return the variable
     */
    static Variable fromParameter(Parameter parameter) {
        return PackedVariable.of(parameter.getParameterizedType(), parameter.getAnnotations());
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
    static Variable fromReturnType(Executable executable) {
        if (executable instanceof Method m) {
            return PackedVariable.of(m.getGenericReturnType(), m.getAnnotations());
        } else {
            Constructor<?> c = (Constructor<?>) executable;
            return PackedVariable.of(c.getDeclaringClass(), c.getAnnotations());
        }
    }

    static Variable of(Type type) {
        return new PackedVariable(PackedAnnotationList.EMPTY, type);
    }

    static Variable of(Type type, Annotation... annotations) {
        return new PackedVariable(PackedAnnotationList.ofUnsafe(annotations), type);
    }
}
