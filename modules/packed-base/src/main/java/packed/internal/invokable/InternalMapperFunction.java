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
package packed.internal.invokable;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;

/** A function that maps the result of another function. */
public class InternalMapperFunction<T, R> extends InternalFunction<R> {

    /** The function we map the result from. */
    private final InternalFunction<T> mapFrom;

    /** The function used to map the result. */
    private final Function<? super T, ? extends R> mapper;

    public InternalMapperFunction(TypeLiteral<R> typeLiteral, InternalFunction<T> mapFrom, Function<? super T, ? extends R> mapper) {
        super(typeLiteral);
        this.mapFrom = requireNonNull(mapFrom, "mapFrom is null");
        this.mapper = requireNonNull(mapper, "mapper is null");
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable R invoke(Object[] params) {
        @Nullable
        T invoke = mapFrom.invoke(params);
        return mapper.apply(invoke);
    }
}
