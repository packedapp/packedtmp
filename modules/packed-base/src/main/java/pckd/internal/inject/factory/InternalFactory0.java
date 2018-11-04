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
package pckd.internal.inject.factory;

import static java.util.Objects.requireNonNull;
import static pckd.internal.util.Formatter.format;

import java.util.List;
import java.util.function.Supplier;

import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.InjectionException;
import app.packed.inject.TypeLiteral;
import app.packed.util.Nullable;

/** An internal factory for {@link Factory0}. */
public final class InternalFactory0<T> extends InternalFactory<T> {

    /** A cache of extracted type variables. */
    private static final ClassValue<TypeLiteral<?>> TYPE_PARAMETERS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected TypeLiteral<?> computeValue(Class<?> type) {
            return TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0);
        }
    };

    /** The supplier that creates the actual objects. */
    private final Supplier<? extends T> supplier;

    public InternalFactory0(Supplier<? extends T> supplier, TypeLiteral<T> typeLiteralOrKey) {
        super(typeLiteralOrKey, List.of());
        this.supplier = requireNonNull(supplier, "supplier is null");
    }

    @Override
    public Class<?> getLowerBound() {
        return Object.class; // The raw supplier generate objects
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public T instantiate(Object[] ignore) {
        T instance = supplier.get();
        if (!getRawType().isInstance(instance)) {
            throw new InjectionException(
                    "The Supplier '" + format(supplier.getClass()) + "' used when creating a Factory0 instance was expected to produce instances of '"
                            + format(getRawType()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
        }
        return instance;
    }

    /**
     * Create
     * 
     * @param <T>
     *            the type of elements the factory produces
     * @param supplier
     *            the supplier that creates the actual values
     * @param extractFrom
     *            the class to extract the type variable from
     * @return the new factory
     */
    @SuppressWarnings("unchecked")
    public static <T> InternalFactory<T> fromTypeVariable(Supplier<? extends T> supplier, Class<?> extractFrom) {
        return new InternalFactory0<>(supplier, (TypeLiteral<T>) TYPE_PARAMETERS.get(extractFrom));
    }
}
