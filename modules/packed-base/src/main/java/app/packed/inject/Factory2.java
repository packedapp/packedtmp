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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Factory} type that takes two dependencies and uses a {@link BiFunction} to create new instances. The input
 * to the bi-function being the two dependencies.
 * <p>
 */
public abstract class Factory2<T, U, R> extends Factory<R> {

    /** The supplier to delegate to */
    final BiFunction<? super T, ? super U, ? extends R> function;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Factory2(BiFunction<? super T, ? super U, ? extends R> function) {
        super((Supplier) null);
        this.function = requireNonNull(function, "function is null");
    }

    public static <T, R> Factory<R> ofOptional(Function<Optional<? super T>, ? extends R> function, Class<T> dependencyType, Class<R> objectType) {
        throw new UnsupportedOperationException();
    }

    public static <T, U, R> Factory<R> ofOptionalBoth(BiFunction<Optional<? super T>, Optional<? super U>, ? extends R> function, Class<T> dependencyType,
            Class<R> objectType) {
        throw new UnsupportedOperationException();
    }
}
