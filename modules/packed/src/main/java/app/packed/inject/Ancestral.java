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
package app.packed.inject;

import java.util.NoSuchElementException;

import app.packed.base.Nullable;

/**
 *
 */
// Available from Extension + ExtensionBean

// Maybe Ancestor instead.. That is usefull for extension beans
// RawHook... Check extensionBean...
// Nahh taenker det er en keybased injection...
public final /* value */ class Ancestral<T> {

    /** Common instance for {@code root()}. */
    private static final Ancestral<?> ROOT = new Ancestral<>(null);

    /** Ancestor, or {@code null} if root. */
    @Nullable
    private final T ancestor;

    private Ancestral(T ancestor) {
        this.ancestor = ancestor;
    }

    public T ancestorOrElseThrow() {
        if (ancestor == null) {
            throw new NoSuchElementException("No ancestor available");
        }
        return ancestor;
    }

    @Nullable
    public T ancestorOrNull() {
        return ancestor;
    }

    public boolean isAncestor() {
        return ancestor != null;
    }

    public boolean isRoot() {
        return ancestor == null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Ancestral<T> ofNullable(T ancestor) {
        return ancestor == null ? (Ancestral<T>) ROOT : new Ancestral<>(ancestor);
    }

    @SuppressWarnings("unchecked")
    public static <T> Ancestral<T> root() {
        return (Ancestral<T>) ROOT;
    }
}
