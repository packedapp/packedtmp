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
package app.packed.hook;

import java.lang.invoke.MethodHandles.Lookup;

/**
 * A marker interface
 * 
 * The base hooks available in this package.
 */

// Relations to AOP???
public interface Hook {

    /**
     *
     * Must have at least one method annotated with {@link OnHook}.
     */
    interface Builder<T extends Hook> {

        /**
         * Invoked by the runtime when all relevant methods annotated with {@link OnHook} has been called.
         * 
         * @return the hook group that was built.
         */
        T build();

        /**
         * A utility method that tests class that. Mainly used for testing. Instead of needing to spin up a container.
         * 
         * @param <T>
         *            the type of hook group to generate
         * @param hookType
         *            the builder type to instantiate
         * @param caller
         *            a lookup object that has permissions to instantiate the builder and access its and the targets hookable
         *            methods.
         * @param target
         *            the target class that should be processed be specified
         * @return a new group
         */
        static <T extends Hook> T test(Lookup caller, Class<T> hookType, Class<?> target) {
            throw new UnsupportedOperationException();
        }
    }
}
