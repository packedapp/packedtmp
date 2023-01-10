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
package internal.app.packed.util.sandbox;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

import app.packed.binding.Variable;

/**
 * A wrapper for the type part of a {@link Variable}.
 */
public interface VariableTypeWrapper {

    Optional<String> name();
    
    Class<?> rawType();

    Type type();
    
    record OfClass(Class<?> clazz) implements VariableTypeWrapper {
        public OfClass {
            requireNonNull(clazz, "clazz is null");
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return clazz;
        }

        public Optional<String> name() {
            return Optional.empty();
        }

        public String toString() {
            return clazz.getCanonicalName();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return rawType();
        }
    }

    record OfTypeVariable(TypeVariable<?> typeVariable) implements VariableTypeWrapper {

        public OfTypeVariable {
            requireNonNull(typeVariable, "typeVariable is null");
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return typeVariable.getGenericDeclaration().getClass();
        }

        public String toString() {
            return typeVariable.toString();
        }

        public Optional<String> name() {
            return Optional.of(typeVariable.getName());
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return typeVariable;
        }
    }

    record OfParameter(Parameter parameter) implements VariableTypeWrapper {

        public OfParameter {
            requireNonNull(parameter, "parameter is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(parameter.getName());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return parameter.getType();
        }

        public String toString() {
            return parameter.getParameterizedType().toString();
        }

        /** {@inheritDoc} */
        @Override
        public Type type() {
            return parameter.getParameterizedType();
        }
    }

    record OfConstructor(Constructor<?> constructor) implements VariableTypeWrapper {

        public OfConstructor {
            requireNonNull(constructor, "constructor is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(constructor.getName());
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

    record OfMethodReturnType(Method method) implements VariableTypeWrapper {

        public OfMethodReturnType {
            requireNonNull(method, "method is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(method.getName()); // ??? or returnVar? IDK
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

    record OfField(Field field) implements VariableTypeWrapper {

        public OfField {
            requireNonNull(field, "field is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(field.getName());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> rawType() {
            return field.getType();
        }

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
