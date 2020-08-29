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

import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import app.packed.base.Attribute;
import app.packed.base.AttributeMap;
import app.packed.base.Nullable;

/**
 *
 */
public final class EmptyAttributeSet implements AttributeMap {

    public static final EmptyAttributeSet EMPTY = new EmptyAttributeSet();

    private EmptyAttributeSet() {}

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Attribute<?>, Object>> entrySet() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    public <T> T get(Attribute<T> attribute) {
        throw new NoSuchElementException();
    }

    /** {@inheritDoc} */
    @Override
    public <T> void ifPresent(Attribute<T> attribute, Consumer<? super T> action) {}

    /** {@inheritDoc} */
    @Override
    public <T> void ifPresentOrElse(Attribute<T> attribute, Consumer<? super T> action, Runnable emptyAction) {
        emptyAction.run();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(Attribute<?> attribute) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Attribute<?>> keys() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    public <T, U> Optional<U> map(Attribute<T> attribute, Function<? super T, ? extends U> mapper) {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public <T> T orElse(Attribute<T> attribute, @Nullable T other) {
        return other;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public <T> T orElseGet(Attribute<T> attribute, Supplier<? extends T> supplier) {
        return supplier.get();
    }

    /** {@inheritDoc} */
    @Override
    public <T> boolean testIfPresent(Attribute<T> attribute, Predicate<? super T> predicate, boolean defaultValue) {
        return defaultValue;
    }
}
