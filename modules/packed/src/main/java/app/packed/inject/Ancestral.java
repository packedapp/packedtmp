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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Nullable;

/**
 *
 * This class is currently used by Packed in two places.
 * 
 * It allows an extension to inject a parent extension if available
 * 
 * It allows an extension bean to inject an ancestor extension bean of the same type as itself.
 * 
 * 
 * @apiNote Something about optional having a special meaning
 */
// Available from Extension + ExtensionBean

// Maybe Ancestor instead.. That is usefull for extension beans
// RawHook... Check extensionBean...
// Nahh taenker det er en keybased injection...

// Maaske kunne vi have en general family??
// Doo<List<Children>> idk
public final /* value */ class Ancestral<T> {

    /** Shared instance for {@code root()}. */
    private static final Ancestral<?> ROOT = new Ancestral<>(null);

    /** Ancestor, or {@code null} if root. */
    @Nullable
    private final T ancestor;

    private Ancestral(T ancestor) {
        this.ancestor = ancestor;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result produced by the supplying function.
     *
     * @param supplier
     *            the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the supplying function
     * @throws NullPointerException
     *             if no value is present and the supplying function is {@code null}
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        return ancestor != null ? ancestor : supplier.get();
    }

    public <U> Ancestral<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isRoot()) {
            return root();
        } else {
            return Ancestral.ofNullable(mapper.apply(ancestor));
        }
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