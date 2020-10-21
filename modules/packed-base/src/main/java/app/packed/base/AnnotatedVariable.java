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
package app.packed.base;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

import packed.internal.component.variable.FieldVariable;
import packed.internal.component.variable.ParameterVariable;

/**
 * {@code AnnotatedVariable} represents a type and an annotated element. A variable is simple wrapper around a
 * {@link TypeToken} and an {@link AnnotatedElement}. A variable is typically constructed from a {@link Field},
 * {@link Parameter}, {@link TypeVariable} or the return type of a {@link Method}. But it can also be synthetically
 * constructed.
 */
// javadoc from AnnotatedTypeVariable
public interface AnnotatedVariable extends AnnotatedElement {

    /**
     * Returns whether or not a {@link Nullable} annotation is present on the variable.
     * 
     * @return true if a nullable annotation is present, otherwise false
     */
    default boolean isNullable() {
        return isAnnotationPresent(Nullable.class);
    }

    /**
     * A variable may declare a name.
     * 
     * @return the name of the variable, or empty if the variable does not have a name
     */
    Optional<String> name(); // Should mainly be used for debug reasons.

    /**
     * Returns the raw type (Class) of the variable.
     * 
     * @return the raw type of the variable
     */
    Class<?> rawType();

    default Optional<?> source() {
        // Parameter, Field, Method (return type), Type Variable
        return Optional.empty();
    }

    TypeToken<?> type();

    static AnnotatedVariable ofField(Field field) {
        requireNonNull(field, "field is null");
        return new FieldVariable(field);
    }

    static AnnotatedVariable ofMethodReturnType(Method method) {
        throw new UnsupportedOperationException();
    }

    static AnnotatedVariable ofParameter(Parameter parameter) {
        requireNonNull(parameter, "parameter is null");
        return new ParameterVariable(parameter);
    }
}
// maybe just have Optional<?> source()

// TypeVariable
// Field
// Parameter
// Synthetic
// Transformed

// Syntes sgu ikke den har et navn...