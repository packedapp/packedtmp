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
package packed.internal.reflect;

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
// Enten som en Builder, eller noget a.la. Bundle.
//disableComposites/ignoreComposite/failOnComposite/...
public final class InjectableFunction {

    private final MethodType input;

    final HashMap<Key<?>, Entry> keys = new HashMap<>();

    private InjectableFunction(MethodType input) {
        this.input = requireNonNull(input);
    }

    private InjectableFunction add(Key<?> key, int index, MethodHandle transformer) {
        Objects.checkFromIndexSize(index, 0, input.parameterCount());
        // Check the various types...
        if (keys.putIfAbsent(key, new Entry(index, transformer)) != null) {
            throw new IllegalArgumentException("The specified key " + key + " has already been added");
        }
        return this;
    }

    public InjectableFunction addKey(Class<?> key, int index) {
        return addKey(Key.of(key), index);
    }

    public InjectableFunction addKey(Class<?> key, int index, MethodHandle transformer) {
        return add(Key.of(key), index, transformer);
    }

    public InjectableFunction addKey(Key<?> key, int index) {
        return add(key, index, null);
    }

    public InjectableFunction addKey(Key<?> key, int index, MethodHandle transformer) {
        return add(key, index, requireNonNull(transformer, "transformer is null"));
    }

    public MethodType input() {
        return input;
    }

    public static InjectableFunction of(Class<?> type, Class<?>... parameterTypes) {
        return new InjectableFunction(MethodType.methodType(type, parameterTypes));
    }

    public static InjectableFunction of(MethodType mt) {
        return new InjectableFunction(mt);
    }

    static class Entry {
        int index;
        @Nullable
        MethodHandle transformer;

        Entry(int index, MethodHandle transformer) {
            this.index = index;
            this.transformer = transformer;
        }
    }
}

// Kunne godt have noget hjaelpe tekst. hvis man ikke kan finde en key..
// F.eks. ? extends Extension -> Du skal bruge UseExtension...
// Extension -> Du skal dependende paa en specific extension...
// 