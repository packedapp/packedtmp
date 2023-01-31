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
package app.packed.bindings;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

import internal.app.packed.util.types.TypeUtil;

/**
 * A wrapper for the type part of a {@link Variable}.
 */
sealed interface PackedVariableType {

    // IDK. 
    // Nu er det kun field og parameter
    Optional<String> name();
    
    Class<?> rawType();

    Type type();
    
    record OfType(Type type) implements PackedVariableType {
        public OfType {
            requireNonNull(type, "clazz is null");
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return TypeUtil.rawTypeOf(type);
        }

        @Override
        public Optional<String> name() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return type.getTypeName();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return type;
        }
    }
    record OfClass(Class<?> clazz) implements PackedVariableType {
        public OfClass {
            requireNonNull(clazz, "clazz is null");
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return clazz;
        }

        @Override
        public Optional<String> name() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return clazz.getCanonicalName();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return rawType();
        }
    }

    record OfTypeVariable(TypeVariable<?> typeVariable) implements PackedVariableType {

        public OfTypeVariable {
            requireNonNull(typeVariable, "typeVariable is null");
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return typeVariable.getGenericDeclaration().getClass();
        }

        @Override
        public String toString() {
            return typeVariable.toString();
        }

        @Override
        public Optional<String> name() {
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return typeVariable;
        }
    }

    record OfParameter(Parameter parameter) implements PackedVariableType {

        public OfParameter {
            requireNonNull(parameter, "parameter is null");
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> name() {
            return Optional.of(parameter.getName());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return parameter.getType();
        }

        @Override
        public String toString() {
            return parameter.getParameterizedType().toString();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return parameter.getParameterizedType();
        }
    }

    record OfConstructor(Constructor<?> constructor) implements PackedVariableType {

        public OfConstructor {
            requireNonNull(constructor, "constructor is null");
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> name() {
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return constructor.getDeclaringClass();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return rawType();
        }
    }

    record OfMethodReturnType(Method method) implements PackedVariableType {

        public OfMethodReturnType {
            requireNonNull(method, "method is null");
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> name() {
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return method.getReturnType();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return method.getGenericReturnType();
        }
    }

    record OfField(Field field) implements PackedVariableType {

        public OfField {
            requireNonNull(field, "field is null");
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> name() {
            return Optional.of(field.getName());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return field.getType();
        }

        @Override
        public String toString() {
            return field.getType().toString();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return field.getGenericType();
        }
    }
}
