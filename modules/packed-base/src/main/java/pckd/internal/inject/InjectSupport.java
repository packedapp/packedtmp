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
package pckd.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import app.packed.inject.Factory;
import app.packed.inject.Key;
import app.packed.inject.TypeLiteral;
import pckd.internal.inject.factory.InternalFactory;

/** A support class for calling package private methods in the app.packed.inject package. */
public final class InjectSupport {

    /**
     * Extracts the internal factory from the specified factory
     * 
     * @param <T>
     *            the type of elements the factory produces
     * @param factory
     *            the factory to extract from
     * @return the internal factory
     */
    public static <T> InternalFactory<T> toInternalFactory(Factory<T> factory) {
        return SingletonHolder.SINGLETON.toInternalFactory(factory);
    }

    public static Key<?> toKeyNullableQualifier(Type type, Annotation qualifier) {
        return SingletonHolder.SINGLETON.toKeyNullableQualifier(type, qualifier);
    }

    public static TypeLiteral<?> toTypeLiteral(Type type) {
        return SingletonHolder.SINGLETON.toTypeLiteral(type);
    }

    /** Holder of the singleton. */
    static class SingletonHolder {

        /** The singleton instance. */
        static final Helper SINGLETON;

        static {
            TypeLiteral.of(Object.class); // Initializes TypeLiteral, which in turn will call SupportInject#init
            SINGLETON = requireNonNull(Helper.SUPPORT, "internal error");
        }
    }

    /** An abstract class that must be implemented by a class in app.packed.inject. */
    public static abstract class Helper {

        /** An instance of the single implementation of this class. */
        private static Helper SUPPORT;

        /**
         * Extracts the internal factory from the specified factory
         * 
         * @param <T>
         *            the type of elements the factory produces
         * @param factory
         *            the factory to extract from
         * @return the internal factory
         */
        protected abstract <T> InternalFactory<T> toInternalFactory(Factory<T> factory);

        // Take a Source??? For example, a method to use for error message.
        // When creating the key
        protected abstract Key<?> toKeyNullableQualifier(Type type, Annotation qualifier);

        /**
         * Converts the type to a type literal.
         * 
         * @param type
         *            the type to convert
         * @return the type literal
         */
        protected abstract TypeLiteral<?> toTypeLiteral(Type type);

        /**
         * Initializes this class.
         * 
         * @param support
         *            an implementation of this class
         */
        public static void init(Helper support) {
            if (SUPPORT != null) {
                throw new Error("Can only be initialized ince");
            }
            SUPPORT = requireNonNull(support);
        }
    }
}
