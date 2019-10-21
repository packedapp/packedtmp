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
package packed.internal.hook.model;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;

import packed.internal.container.access.ClassProcessor;

/**
 *
 */
final class OnHookNodeBuilder {

    /** The class processor used for iterating over methods. */
    final ClassProcessor cp;

    /** The i */
    int id;

    Set<OnHookNodeBuilder> dependencies;

    OnHookNodeBuilder(ClassProcessor cp) {
        this.cp = requireNonNull(cp);
    }

    void addDependency(OnHookNodeBuilder b) {
        Set<OnHookNodeBuilder> d = dependencies;
        if (d == null) {
            d = dependencies = new HashSet<>();
        }
        d.add(b);
    }

    boolean hasUnresolvedDependencies() {
        if (dependencies != null) {
            for (OnHookNodeBuilder ch : dependencies) {
                if (ch.id == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}

// STEP 1
/// Find All methods
/// Validate Parameters
/// Go into dependencies...

// Find them, validate parameters
// Validate we can make MethodHandle

// Step 2
// Validate no
