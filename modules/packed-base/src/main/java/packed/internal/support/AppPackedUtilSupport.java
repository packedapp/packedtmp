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
package packed.internal.support;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import app.packed.util.Key;
import app.packed.util.TypeLiteral;

/** A support class for calling package private methods in the app.packed.util package. */
public final class AppPackedUtilSupport {

    public static Helper invoke() {
        return SingletonHolder.SINGLETON;
    }

    /** Holder of the singleton. */
    private static class SingletonHolder {

        /** The singleton instance. */
        static final Helper SINGLETON;

        static {
            TypeLiteral.of(Object.class); // Initializes TypeLiteral, which in turn will call Helper#init
            SINGLETON = requireNonNull(Helper.SUPPORT, "internal error");
        }
    }

    /** An abstract class that must be implemented by a class in app.packed.inject. */
    public static abstract class Helper {

        /** An instance of the single implementation of this class. */
        private static Helper SUPPORT;

        // Take a Source??? For example, a method to use for error message.
        // When creating the key
        public abstract Key<?> toKeyNullableQualifier(Type type, Annotation qualifier);

        /**
         * Converts the type to a type literal.
         * 
         * @param type
         *            the type to convert
         * @return the type literal
         */
        public abstract TypeLiteral<?> toTypeLiteral(Type type);

        public abstract boolean isCanonicalized(TypeLiteral<?> typeLiteral);

        /**
         * Initializes this class.
         * 
         * @param support
         *            an implementation of this class
         */
        public static synchronized void init(Helper support) {
            if (SUPPORT != null) {
                throw new ExceptionInInitializerError("Can only be initialized once");
            }
            SUPPORT = requireNonNull(support);
        }
    }
}
