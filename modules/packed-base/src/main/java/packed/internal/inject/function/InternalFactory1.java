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
package packed.internal.inject.function;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.util.function.Function;

import app.packed.inject.Factory1;
import app.packed.inject.InjectionException;
import app.packed.inject.TypeLiteral;
import app.packed.util.Nullable;

/** An internal factory for {@link Factory1}. */
public class InternalFactory1<T, R> extends InternalFunction<R> {

    /** The function that creates the actual objects. */
    private final Function<? super T, ? extends R> function;

    /**
     * @param supplier
     * @param functionalSignature
     */
    public InternalFactory1(TypeLiteral<R> type, Function<? super T, ? extends R> function) {
        super(type);
        this.function = requireNonNull(function, "function is null");
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public R invoke(Object[] params) {
        T t = (T) params[0];
        R instance = function.apply(t);
        if (!getRawType().isInstance(instance)) {
            throw new InjectionException(
                    "The Function '" + format(function.getClass()) + "' used when creating a Factory1 instance was expected to produce instances of '"
                            + format(getRawType()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
        }
        return instance;
    }
}
