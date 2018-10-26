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

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 */
public abstract class Factory2<T, U, R> extends Factory<R> {

    /** A cache of function factory definitions. */
    static final ClassValue<CachedFactoryDefinition> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected CachedFactoryDefinition computeValue(Class<?> type) {
            TypeLiteral<?> tl = TypeLiteral.fromTypeVariable(Factory.class, 0, (Class) type);
            Key<?> d1 = Key.getKeyOfArgument(Factory2.class, 0, (Class) type);
            Key<?> d2 = Key.getKeyOfArgument(Factory2.class, 1, (Class) type);
            return new CachedFactoryDefinition(tl, List.of(new Dependency(d1, null, 0), new Dependency(d2, null, 1)));
        }
    };

    /** The supplier to delegate to */
    final BiFunction<? super T, ? super U, ? extends R> function;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Factory2(BiFunction<? super T, ? super U, ? extends R> function) {
        super((Supplier) null);
        this.function = requireNonNull(function, "function is null");
    }

    // @Override
    // @SuppressWarnings("unchecked")
    // protected final R create(Object[] args) {
    // T p = (T) args[0];
    // U u = (U) args[1];
    // return function.apply(p, u);
    // }

    public static <T, R> Factory<R> ofOptional(Function<Optional<? super T>, ? extends R> function, Class<T> dependencyType, Class<R> objectType) {
        throw new UnsupportedOperationException();
    }

    public static <T, U, R> Factory<R> ofOptionalBoth(BiFunction<Optional<? super T>, Optional<? super U>, ? extends R> function, Class<T> dependencyType,
            Class<R> objectType) {
        throw new UnsupportedOperationException();
    }

    static class CachedFactoryDefinition {
        final List<Dependency> dependencies;

        final TypeLiteral<?> objectType;

        CachedFactoryDefinition(TypeLiteral<?> objectType, List<Dependency> dependencies) {
            this.objectType = requireNonNull(objectType);
            this.dependencies = requireNonNull(dependencies);
        }
    }

}
