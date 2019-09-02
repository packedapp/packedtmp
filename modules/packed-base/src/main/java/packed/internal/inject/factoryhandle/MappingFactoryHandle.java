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
package packed.internal.inject.factoryhandle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import app.packed.util.TypeLiteral;

/** A function that maps the result of another function. */
public final class MappingFactoryHandle<T, R> extends FactoryHandle<R> {

    /** The function we map the result from. */
    final FactoryHandle<T> mapFrom;

    /** The function used to map the result. */
    final Function<? super T, ? extends R> mapper;

    public MappingFactoryHandle(TypeLiteral<R> typeLiteral, FactoryHandle<T> mapFrom, Function<? super T, ? extends R> mapper) {
        super(typeLiteral);
        this.mapFrom = requireNonNull(mapFrom, "mapFrom is null");
        this.mapper = requireNonNull(mapper, "mapper is null");
    }

    // /** {@inheritDoc} */
    // @Override
    // public @Nullable R invoke(Object[] params) {
    // throw new UnsupportedOperationException();
    // }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle() {
        // MethodHandle mh = mapFrom.toMethodHandle();
        // Look for methods in MethodHandles that takes two MethodHandle

        throw new UnsupportedOperationException();
    }
}
