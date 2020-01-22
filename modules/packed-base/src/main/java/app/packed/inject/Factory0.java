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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import app.packed.base.InvalidDeclarationException;
import app.packed.base.TypeLiteral;
import packed.internal.inject.factoryhandle.Factory0FactoryHandle;
import packed.internal.inject.factoryhandle.FactorySupport;

/**
 * A special {@link Factory} type that uses the supplied value from a {@link Supplier} to dynamically provide new
 * instances.
 * 
 * <p>
 * Is typically used like this:
 *
 * <pre> {@code
 * Factory<Long> f = new Factory0<>(System::currentTimeMillis) {};}</pre>
 * <p>
 * In this example we create a new class inheriting from Factory0 is order to capture information about the suppliers
 * type variable (in this case {@code Long}). Thereby circumventing the limitations of Java's type system for retaining
 * type information at runtime.
 * 
 * @param <R>
 *            the type of objects this factory constructs
 * @see Factory1
 * @see Factory2
 */
public abstract class Factory0<R> extends Factory<R> {

    /** A cache of extracted type variables from subclasses of this class. */
    private static final ClassValue<TypeLiteral<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected TypeLiteral<?> computeValue(Class<?> type) {
            return TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0);
        }
    };

    /**
     * Creates a new factory, that uses the specified supplier to create new instances.
     *
     * @param supplier
     *            the supplier to use for creating new instances. The supplier should never return null, but should instead
     *            throw an exception if unable to provide a value
     * @throws InvalidDeclarationException
     *             if the type variable R could not be determined. Or if R does not represent a valid key, for example,
     *             {@link Optional}
     */
    protected Factory0(Supplier<? extends R> supplier) {
        super(supplier);
    }

    /**
     * Creates a new factory support instance from an implementation of this class and a supplier.
     * 
     * @param implementation
     *            the class extending this class
     * @param supplier
     *            the supplier used for creating new values
     * @return a new factory support instance
     */
    @SuppressWarnings("unchecked")
    static <T> FactorySupport<T> create(Class<?> implementation, Supplier<? extends T> supplier) {
        TypeLiteral<T> tt = (TypeLiteral<T>) CACHE.get(implementation);
        return new FactorySupport<>(new Factory0FactoryHandle<>(tt, supplier), List.of());
    }
}
