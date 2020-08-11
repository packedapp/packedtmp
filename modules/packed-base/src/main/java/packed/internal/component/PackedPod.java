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

import java.util.concurrent.ConcurrentHashMap;

/**
 * All strongly connected components relate to the same pod.
 */
public final class PackedPod {

    PackedPod parent; // Vi kan vel bare smide den i instances...

    ComponentRuntimeDescriptor[] descriptors;// packed descriptors...

    Object[] instances; // May contain f.eks. CHM.. ?? Maybe hosts are also there...
    // If non-root instances[0] always is the parent...

    ConcurrentHashMap<Integer, PackedPod>[] hosts;

    PackedPod() {

    }
}

/// GUESTS (

// En guest kunne mere eller mindre vaere 10 objects