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
import static packed.internal.util.StringFormatter.format;

import java.util.function.BiFunction;

import app.packed.inject.Factory2;
import app.packed.inject.InjectionException;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;

/** An internal factory for {@link Factory2}. */
public class Function2Invokable<T, U, R> extends InternalFunction<R> {

    /** The function responsible for creating the actual objects. */
    private final BiFunction<? super T, ? super U, ? extends R> function;

    public Function2Invokable(TypeLiteral<R> typeLiteral, BiFunction<? super T, ? super U, ? extends R> function) {
        super(typeLiteral);
        this.function = requireNonNull(function);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public R invoke(Object[] params) {
        T t = (T) params[0];
        U u = (U) params[1];
        R instance = function.apply(t, u);
        if (!getReturnTypeRaw().isInstance(instance)) {
            throw new InjectionException(
                    "The BiFunction '" + format(function.getClass()) + "' used when creating a Factory2 instance was expected to produce instances of '"
                            + format(getReturnTypeRaw()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
        }
        return instance;
    }

}
