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

import app.packed.inject.Factory;
import app.packed.inject.InjectionExtension;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.invoke.FunctionHandle;

/** A support class for calling package private methods in the app.packed.inject package. */
public final class AppPackedInjectAccess {

    /**
     * Extracts the internal factory from the specified factory
     * 
     * @param <T>
     *            the type of elements the factory produces
     * @param factory
     *            the factory to extract from
     * @return the internal factory
     */
    public static <T> FunctionHandle<T> toInternalFunction(Factory<T> factory) {
        return SingletonHolder.SINGLETON.toInternalFunction(factory);
    }

    public static Helper invoke() {
        return SingletonHolder.SINGLETON;
    }

    /** Holder of the singleton. */
    static class SingletonHolder {

        /** The singleton instance. */
        static final Helper SINGLETON;

        static {
            Factory.ofInstance("foo");
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
        public abstract <T> FunctionHandle<T> toInternalFunction(Factory<T> factory);

        public abstract InjectorBuilder getBuilder(InjectionExtension ie);

        /**
         * Initializes this class.
         * 
         * @param support
         *            an implementation of this class
         */
        public static void init(Helper support) {
            if (SUPPORT != null) {
                throw new Error("Can only be initialized once");
            }
            SUPPORT = requireNonNull(support);
        }
    }
}
