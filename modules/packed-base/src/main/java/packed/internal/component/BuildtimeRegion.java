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
package packed.internal.component;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import packed.internal.component.source.SourceBuild;
import packed.internal.inject.Dependant;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Vi gemmer alt det her i en region...
// Fordi raekkefoelgen af initialisering gaar paa tvaers af containere
// Idet de kan dependende paa hinanden
public final class BuildtimeRegion {

    /** Components that contains constants that should be stored in a region. Is only written by {@link SourceBuild}. */
    public final ArrayList<SourceBuild> constants = new ArrayList<>();

    // List of services that must be instantiated and stored in the region
    // They are ordered in the order they should be initialized
    // For now written by DependencyCycleDetector via BFS
    public final ArrayList<Dependant> regionStores = new ArrayList<>();

    private int nextIndex;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    public final Lifecycle lifecycle = new Lifecycle();

    RuntimeRegion newRegion(PackedInitializationContext pic, PackedComponent root) {
        RuntimeRegion region = new RuntimeRegion(nextIndex);

        // Not sure we want to create the guest here, we do it for now though
        if (root.modifiers().isContainer()) {
            region.store(0, new PackedContainer(pic));
        }

        // We start by storing all constant instances in the region array
        for (SourceBuild sa : constants) {
            region.store(sa.regionIndex, sa.instance);
        }

        // All constants that must be instantiated and stored
        // Order here is very important. As for every constant.
        // Its dependencies are guaranteed to have been already stored
        for (Dependant injectable : regionStores) {
            MethodHandle mh = injectable.buildMethodHandle();

            Object instance;
            try {
                instance = mh.invoke(region);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }

            if (instance == null) {
                throw new NullPointerException(injectable + " returned null");
            }

            int index = injectable.regionIndex();
            region.store(index, instance);
        }

        // Initialize
        for (MethodHandle mh : initializers) {
            try {
                mh.invoke(region);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
        return region;
    }

    public int reserve() {
        return nextIndex++;
    }

    public static class Lifecycle {

        public boolean hasExecutionBlock() {
            return methodHandle != null;
        }

        public MethodHandle methodHandle;
    }
}
