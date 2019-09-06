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
package packed.internal.inject.util.nextapi;

import java.util.Objects;
import java.util.function.Predicate;

import app.packed.util.Key;

/**
 *
 */
// Or KeySelector...
interface KeyFilter extends Predicate<Key<?>> {

    @Override
    public abstract boolean test(Key<?> key);

    public abstract boolean test(Class<?> key);

    default KeyFilter and(KeyFilter other) {
        Objects.requireNonNull(other);
        throw new UnsupportedOperationException();
        // return (t) -> test(t) && other.test(t);
    }

    static KeyFilter anyOf(Key<?>... keys) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        KeyFilter kf = anyOf(Key.of(String.class)).and(anyOf(Key.of(Integer.class)));
    }

    // KeyFilter not()
    // static anyOf (Class....)

    // static qualifiedWith(Class)
    // static qualifiedWith(Class, Predicate<? extends Annotation> )

    // static inModule("Sddd");
    // static inContract("Sddd");

}
