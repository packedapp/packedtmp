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
package packed.internal.invoke.lambda;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.util.function.Supplier;

import app.packed.inject.Factory0;
import app.packed.inject.InjectionException;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.invoke.FunctionHandle;

/**
 * An function handle that wraps a {@link Supplier}. Is used, for example, from {@link Factory0}.
 * 
 * @param <T>
 *            the type of elements the factory produces
 */
public final class SupplierFunctionHandle<T> extends FunctionHandle<T> {

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
    public SupplierFunctionHandle(TypeLiteral<T> type, Supplier<? extends T> supplier) {
        super(type);
        this.supplier = requireNonNull(supplier, "supplier is null");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public T invoke(Object[] ignore) {
        T instance = supplier.get();
        if (!getReturnTypeRaw().isInstance(instance)) {
            throw new InjectionException(
                    "The Supplier '" + format(supplier.getClass()) + "' used when creating a Factory0 instance was expected to produce instances of '"
                            + format(getReturnTypeRaw()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
        }
        return instance;
    }
}
