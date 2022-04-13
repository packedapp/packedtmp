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
package packed.internal.base;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

import app.packed.base.TypeToken;
import packed.internal.util.ReflectionUtil;

/**
 *
 */
public interface TypeWrapper {
    
    Class<?> getType();

    TypeToken<?> typeToken();

    record OfTypeVariable(TypeVariable<?> typeVariable) implements TypeWrapper {

        public OfTypeVariable {
            requireNonNull(typeVariable, "typeVariable is null");
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return typeVariable.getGenericDeclaration().getClass();
        }

        public Optional<String> name() {
            return Optional.of(typeVariable.getName());
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<?> typeToken() {
            throw new UnsupportedOperationException();
        }
    }

    record OfParameter(Parameter parameter) implements TypeWrapper {

        public OfParameter {
            requireNonNull(parameter, "parameter is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(parameter.getName());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return parameter.getType();
        }

        /** {@inheritDoc} */
        public TypeToken<?> typeToken() {
            Type t = ReflectionUtil.getParameterizedType(parameter, ReflectionUtil.getIndex(parameter));
            return TypeToken.fromType(t);
        }
    }

    record OfConstructor(Constructor<?> constructor) implements TypeWrapper {

        public OfConstructor {
            requireNonNull(constructor, "constructor is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(constructor.getName()); // ??? or returnVar? IDK
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return constructor.getDeclaringClass();
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<?> typeToken() {
            throw new UnsupportedOperationException();
        }
    }

    record OfMethodReturnType(Method method) implements TypeWrapper {

        public OfMethodReturnType {
            requireNonNull(method, "method is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(method.getName()); // ??? or returnVar? IDK
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return method.getReturnType();
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<?> typeToken() {
            return TypeToken.fromMethodReturnType(method);
        }
    }

    record OfField(Field field) implements TypeWrapper {

        public OfField {
            requireNonNull(field, "field is null");
        }

        /** {@inheritDoc} */
        public Optional<String> name() {
            return Optional.of(field.getName());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getType() {
            return field.getType();
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<?> typeToken() {
            return TypeToken.fromField(field);
        }
    }
}
