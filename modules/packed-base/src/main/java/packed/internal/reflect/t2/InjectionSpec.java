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
package packed.internal.reflect.t2;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Objects;

import app.packed.base.Key;
import app.packed.base.Nullable;

/**
 *
 */
public class InjectionSpec {

    final HashMap<Key<?>, Entry> keys = new HashMap<>();

    private final MethodType input;

    public InjectionSpec(Class<?> type, Class<?>... parameterTypes) {
        this.input = MethodType.methodType(type, parameterTypes);
    }

    public InjectionSpec(MethodType input) {
        this.input = requireNonNull(input);
    }

    public InjectionSpec add(Class<?> key, int index) {
        Objects.checkFromIndexSize(index, 0, input.parameterCount());
        // Class<?> c = input.parameterType(index);
        Key<?> k = Key.of(key);
        keys.put(k, new Entry(k, index, null));
        return this;
    }

    public MethodType input() {
        return input;
    }

    public InjectionSpec add(Class<?> key, int fromIndex, MethodHandle transformer) {
        Key<?> k = Key.of(key);
        keys.put(k, new Entry(k, fromIndex, transformer));
        return this;
    }

    static class Entry {
        Key<?> key; // do we need it in the entry...
        int index;
        @Nullable
        MethodHandle transformer;

        Entry(Key<?> key, int index, MethodHandle transformer) {
            this.key = key;
            this.index = index;
            this.transformer = transformer;
        }
    }
}

// Kunne godt have noget hjaelpe tekst. hvis man ikke kan finde en key..
// F.eks. ? extends Extension -> Du skal bruge UseExtension...
// Extension -> Du skal dependende paa en specific extension...
// 