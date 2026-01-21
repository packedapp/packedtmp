/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.build.hook;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import internal.app.packed.build.hooks.BuildHook;

/**
 * Stores registered {@link Lookup} objects for BuildHook classes that have called
 * {@link BuildHook#openToFramework(Lookup)}.
 */
public final class BuildHookModuleSupport {

    /** Per-class lookup storage. Uses WeakHashMap to allow class unloading. */
    private static final Map<Class<?>, Lookup> REGISTERED_LOOKUPS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private BuildHookModuleSupport() {}

    /**
     * Registers a lookup for a BuildHook class.
     *
     * @param lookup the lookup to register (its lookupClass must extend BuildHook)
     * @throws IllegalArgumentException if the lookup class is not a BuildHook subclass
     * @throws IllegalStateException if a lookup is already registered for this class
     */
    public static void registerLookup(Lookup lookup) {
        Class<?> lookupClass = lookup.lookupClass();

        if (!BuildHook.class.isAssignableFrom(lookupClass)) {
            throw new IllegalArgumentException("Lookup class " + lookupClass.getName()
                    + " must be a subclass of BuildHook");
        }

        Lookup existing = REGISTERED_LOOKUPS.putIfAbsent(lookupClass, lookup);
        if (existing != null) {
            throw new IllegalStateException("A lookup has already been registered for " + lookupClass.getName());
        }
    }

    /**
     * Returns a registered lookup for the specified class, or null if none registered.
     *
     * @param type the BuildHook class to look up
     * @return the registered lookup, or null
     */
    public static Lookup getLookupFor(Class<?> type) {
        return REGISTERED_LOOKUPS.get(type);
    }
}
