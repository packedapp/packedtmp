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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Supplier;

import app.packed.base.TypeLiteral;
import app.packed.inject.Factory0;
import packed.internal.methodhandle.LookupUtil;

/**
 * An function handle that wraps a {@link Supplier}. Is used, for example, from {@link Factory0}.
 * 
 * @param <T>
 *            the type of elements the factory produces
 */
public final class Factory0FactoryHandle<T> extends FactoryHandle<T> {

    /** A cache of extracted type variables from subclasses of this class. */
    private static final ClassValue<TypeLiteral<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected TypeLiteral<?> computeValue(Class<?> type) {
            return TypeLiteral.fromTypeVariable((Class) type, BaseFactory.class, 0);
        }
    };

    /** A method handle for {@link Supplier#get()}. */
    private static final MethodHandle GET = LookupUtil.mhVirtualPublic(Supplier.class, "get", Object.class);

    /** The supplier that creates the actual objects. */
    private final Supplier<? extends T> supplier;

    /**
     * Creates a SupplierFunctionHandle instance.
     * 
     * @param type
     *            the class to extract type info from.
     * @param supplier
     *            the supplier that creates the actual values
     */
    private Factory0FactoryHandle(TypeLiteral<T> type, Supplier<? extends T> supplier) {
        super(type);
        this.supplier = requireNonNull(supplier, "supplier is null");
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle() {
        MethodHandle mh = GET.bindTo(supplier);
        mh = MethodHandles.explicitCastArguments(mh, methodType());
        return mh;
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
    public static <T> FactorySupport<T> create(Class<?> implementation, Supplier<? extends T> supplier) {
        TypeLiteral<T> tt = (TypeLiteral<T>) CACHE.get(implementation);
        return new FactorySupport<>(new Factory0FactoryHandle<>(tt, supplier), List.of());
    }

    /** {@inheritDoc} */
    @Override
    public MethodType methodType() {
        return MethodType.methodType(returnTypeRaw());
    }
}

// /** {@inheritDoc} */
// @Override
// @Nullable
// public T invoke(Object[] ignore) {
// T instance = supplier.get();
// if (!returnTypeRaw().isInstance(instance)) {
// throw new InjectionException(
// "The Supplier '" + format(supplier.getClass()) + "' used when creating a Factory0 instance was expected to produce
// instances of '"
// + format(returnTypeRaw()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
// }
// return instance;
// }
