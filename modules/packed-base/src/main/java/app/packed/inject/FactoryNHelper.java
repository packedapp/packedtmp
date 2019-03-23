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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.util.TypeLiteral;
import packed.internal.inject.InternalDependency;
import packed.internal.invokers.InternalFactory0;
import packed.internal.invokers.InternalFactory1;
import packed.internal.invokers.InternalFactory2;

/** Helper methods for {@link Factory0}, {@link Factory1} and {@link Factory2}. */
class FactoryNHelper {

    /** A cache of extracted type variables. */
    private static final ClassValue<TypeLiteral<?>> FACTORY0_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected TypeLiteral<?> computeValue(Class<?> type) {
            return TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0);
        }
    };

    /** A cache of function factory definitions. */
    private static final ClassValue<FactoryNSignature> FACTORY1_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected FactoryNSignature computeValue(Class<?> type) {
            return new FactoryNSignature(TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0),
                    InternalDependency.fromTypeVariables((Class) type, Factory1.class, 0));
        }
    };
    /** A cache of function factory definitions. */
    private static final ClassValue<FactoryNSignature> FACTORY2_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected FactoryNSignature computeValue(Class<?> type) {
            return new FactoryNSignature(TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0),
                    InternalDependency.fromTypeVariables(Factory2.class, (Class) type, 0, 1));
        }
    };

    @SuppressWarnings("unchecked")
    static <T> InternalFactory<T> create0(Supplier<? extends T> supplier, Class<?> typeInfo) {
        TypeLiteral<T> tt = (TypeLiteral<T>) FACTORY0_CACHE.get(typeInfo);
        return new InternalFactory<>(new InternalFactory0<>(tt, supplier), List.of());
    }

    @SuppressWarnings("unchecked")
    static <T, R> InternalFactory<R> create1(Function<?, ? extends T> supplier, Class<?> typeInfo) {
        FactoryNSignature fs = FACTORY1_CACHE.get(typeInfo);
        return new InternalFactory<>(new InternalFactory1<>((TypeLiteral<R>) fs.objectType, (Function<? super T, ? extends R>) supplier), fs.dependencies);
    }

    @SuppressWarnings("unchecked")
    static <T, U, R> InternalFactory<R> create2(BiFunction<?, ?, ? extends T> supplier, Class<?> typeInfo) {
        FactoryNSignature fs = FACTORY2_CACHE.get(typeInfo);
        return new InternalFactory<>(new InternalFactory2<>((TypeLiteral<R>) fs.objectType, (BiFunction<? super T, ? super U, ? extends R>) supplier),
                fs.dependencies);
    }

    private static class FactoryNSignature {

        private final List<InternalDependency> dependencies;

        private final TypeLiteral<?> objectType;

        FactoryNSignature(TypeLiteral<?> objectType, List<InternalDependency> dependencies) {
            this.objectType = requireNonNull(objectType);
            this.dependencies = requireNonNull(dependencies);
        }
    }
}
