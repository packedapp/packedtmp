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
package app.packed.base;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 *
 */
public class DefaultAttributeMap {

    final HashMap<Attribute<?>, Value> m = new HashMap<>();

    public <T> void addValue(Attribute<T> atr, T value) {
        requireNonNull(atr);
        requireNonNull(value);
        m.put(atr, new PermValue(value));
    }

    public <T> void addSuppliedValue(Attribute<T> atr, Supplier<T> value) {
        requireNonNull(atr);
        requireNonNull(value);
        m.put(atr, new CompValue(value));
    }

    static class CompValue extends Value {
        final Supplier<?> suppler;

        CompValue(Supplier<?> suppler) {
            this.suppler = requireNonNull(suppler);
        }

        /** {@inheritDoc} */
        @Override
        Object get() {
            return suppler.get();
        }
    }

    static class PermValue extends Value {

        final Object o;

        public PermValue(Object o) {
            this.o = o;
        }

        @Override
        Object get() {
            return o;
        }

    }

    abstract static class Value {

        abstract Object get();
    }
}
