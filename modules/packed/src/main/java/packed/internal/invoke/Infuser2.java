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
package packed.internal.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;

import app.packed.base.Key;

/**
 *
 */

// En prototype paa en "naeste" version af Infuser..

// Vi har lanes som er enten parametere. Eller mapping udfra andre lanes

// Vi kan maaske tilfoje en lazy computation. Som kun bliver aktiveret hvis brugt...
// Nej det er vel bare at tilrette mapning? som istedet for at mappe.
// Laver en ny metode med parameteren.

class Infuser2 {

    public static class Builder {

        private final ArrayList<Entry> entries = new ArrayList<>();

        final HashMap<Key<?>, ServiceEntry> services = new HashMap<>();

        MethodType methodType;

        public int addParameter(Class<?> type) {
            requireNonNull(type, "type is null");
            methodType.appendParameterTypes(type);
            entries.add(new ParamEntry(type));
            return entries.size();
        }

        
        public int addMapping(MethodHandle computation, int... dependencies) {
            entries.add(new MappingEntry(computation, dependencies));
            return entries.size();
        }

        public Builder changeReturnType(Class<?> nrtype) {
            methodType.changeReturnType(nrtype);
            return this;
        }

        public Builder provideService(Class<?> key, int laneIndex) {
            return provideService(Key.of(key), laneIndex);
        }

        public Builder provideService(Key<?> key, int laneIndex) {
            // check key is assignable...
            throw new UnsupportedOperationException();
        }
    }

    private record ServiceEntry(Key<?> key, int index) {

    }

    private sealed interface Entry permits ParamEntry, MappingEntry {
        Class<?> type();
    }

    private record ParamEntry(Class<?> type) implements Entry {

    }

    private record MappingEntry(MethodHandle computation, int[] dependencies) implements Entry {

        /** {@inheritDoc} */
        @Override
        public Class<?> type() {
            return computation.type().returnType();
        }
    }
}
