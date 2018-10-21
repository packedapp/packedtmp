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
package packed.util.invokers;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 *
 */
public final class LookupAccessor {

    public static final LookupAccessor PUBLIC = new LookupAccessor(MethodHandles.publicLookup());

    final MethodHandles.Lookup lookup;

    MethodHandle[] methodHandles;

    LookupAccessor(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup);
    }

    public static LookupAccessor get(MethodHandles.Lookup lookup) {
        return new LookupAccessor(lookup);
    }

    // install as component class
    // install as component instance
    // install as mixin class
    // install as mixin instance
    // install as service class
    // install as service instance
    // newInstance()

    // Naar vi laver factoriet ved vi ikke hvordan det skal bruges....
    // Det er heller ikke fordi vi kender noget til Lookup objektet...

    // Saa vi skal have en special struktur til det. Der er uafhaendig af hele invokerings cirkuset....

    // InternalFactory, kan heller ikke vide noget om det, med mindre vi laver en withLookup();

    // Den ved kun noget om Lookup, meeeen

}
