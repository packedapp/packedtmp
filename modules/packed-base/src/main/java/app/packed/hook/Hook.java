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

import app.packed.container.InternalExtensionException;
import app.packed.lang.Nullable;
import packed.internal.hook.model.UseIt2;

/**
 * A marker interface
 * 
 * The base hooks available in this package.
 * 
 * Builds must have at least one method annotated with {@link OnHook}
 */

// Custom hooks must declare a static (with any visibility) class named Builder extending #Builder and the with Hook
// type as type Variable;

// Relations to AOP???
// Rename to Trigger? Ej.
public interface Hook {

    /** A builder for custom hooks, see {@link Hook} for details about how to implement this interface. */
    interface Builder<T extends Hook> {

        /**
         * Invoked by the runtime when all methods annotated with {@link OnHook} has been successfully invoked.
         * 
         * @return the hook that was built.
         */
        T build();

        /**
         * A test method that can be used to easily test custom implemented hooks.
         * <p>
         * Build on example from {@link Hook}, Maybe put everything together
         * 
         * @param <T>
         *            the type of hook group to generate
         * @param hookType
         *            the builder type to instantiate
         * @param caller
         *            a lookup object that can access both the builder of the hook type, and the target.
         * @param target
         *            the target class that should be processed by the builder
         * @return a new hook, or null if no hooks were activated
         * 
         * @throws AssertionError
         *             if something went wrong
         */
        @Nullable
        static <T extends Hook> T test(Lookup caller, Class<T> hookType, Class<?> target) {
            try {
                return UseIt2.test(caller, hookType, target);
            } catch (InternalExtensionException ee) {
                AssertionError ar = new AssertionError(ee.getMessage());
                ar.setStackTrace(ee.getStackTrace());
                throw ar;
            }
        }
    }
}
