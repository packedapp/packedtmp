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
package packed.internal.base.attribute;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.base.Attribute;
import app.packed.base.AttributeMap;

/**
 *
 */
public class DefaultAttributeMap implements AttributeMap {

    final HashMap<Attribute<?>, Value> m = new HashMap<>();

    public <T> void addSuppliedValue(Attribute<T> atr, Supplier<T> value) {
        requireNonNull(atr);
        requireNonNull(value);
        m.put(atr, new CompValue(value));
    }

    public <T> void addValue(Attribute<T> atr, T value) {
        requireNonNull(atr);
        requireNonNull(value);
        m.put(atr, new PermValue(value));
    }

    /** {@inheritDoc} */
    @Override
    public <A> A get(Attribute<A> attribute) {
        return getOpt(attribute).get();
    }

    public <T> Optional<T> getOpt(Attribute<T> attribute) {
        Value v = m.get(attribute);
        if (v == null) {
            return Optional.empty();
        } else {
            @SuppressWarnings("unchecked")
            T t = (T) v.get();
            return Optional.of(t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(Attribute<?> attribute) {
        return getOpt(attribute).isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Attribute<?>> keys() {
        return Collections.unmodifiableSet(m.keySet());
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

    public static DefaultAttributeMap from() {
        throw new UnsupportedOperationException();
    }
}
