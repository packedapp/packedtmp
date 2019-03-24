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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import app.packed.util.InvalidDeclarationException;
import app.packed.util.TypeLiteral;
import packed.internal.inject.InternalDependency;
import packed.internal.invokers.InternalFactory2;

/**
 * A {@link Factory} type that takes two dependencies and uses a {@link BiFunction} to create new instances. The input
 * to the bi-function being the two dependencies.
 * 
 * @see Factory0
 * @see Factory1
 */
public abstract class Factory2<T, U, R> extends Factory<R> {

    /**
     * Creates a new factory.
     *
     * @param function
     *            the function to use for creating new instances
     * @throws InvalidDeclarationException
     *             if the type variable T, U or R could not be determined. Or if T or U does not represent a proper
     *             dependency, or R does not represent a proper key
     */
    protected Factory2(BiFunction<? super T, ? super U, ? extends R> function) {
        super(function);
    }

    /** A cache of function factory definitions. */
    private static final ClassValue<Entry<TypeLiteral<?>, List<InternalDependency>>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Entry<TypeLiteral<?>, List<InternalDependency>> computeValue(Class<?> type) {
            return new SimpleImmutableEntry<>(TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0),
                    InternalDependency.fromTypeVariables(Factory2.class, (Class) type, 0, 1));
        }
    };

    @SuppressWarnings("unchecked")
    static <T, U, R> InternalFactory<R> create(BiFunction<?, ?, ? extends T> supplier, Class<?> typeInfo) {
        Entry<TypeLiteral<?>, List<InternalDependency>> fs = CACHE.get(typeInfo);
        return new InternalFactory<>(new InternalFactory2<>((TypeLiteral<R>) fs.getKey(), (BiFunction<? super T, ? super U, ? extends R>) supplier),
                fs.getValue());
    }
}
