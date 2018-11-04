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
import static packed.internal.util.StringFormatter.format;

import java.util.List;
import java.util.function.Function;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.Factory1;
import app.packed.inject.InjectionException;
import app.packed.inject.TypeLiteral;
import app.packed.util.Nullable;

/** An internal factory for {@link Factory1}. */
public class InternalFactory1<T, R> extends InternalFactory<R> {

    /** A cache of function factory definitions. */
    static final ClassValue<FunctionalSignature> TYPE_PARAMETERS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected FunctionalSignature computeValue(Class<?> type) {
            return new FunctionalSignature(TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0),
                    Dependency.fromTypeVariables(Factory1.class, (Class) type, 0));
        }
    };

    /** The function that creates the actual objects. */
    private final Function<? super T, ? extends R> function;

    public InternalFactory1(Function<? super T, ? extends R> function, TypeLiteral<R> typeLiteral, List<Dependency> dependencies) {
        super(typeLiteral, dependencies);
        this.function = requireNonNull(function);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getLowerBound() {
        return Object.class; // The raw function return objects
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public R instantiate(Object[] params) {
        T t = (T) params[0];
        R instance = function.apply(t);
        if (!getRawType().isInstance(instance)) {
            throw new InjectionException(
                    "The Function '" + format(function.getClass()) + "' used when creating a Factory1 instance was expected to produce instances of '"
                            + format(getRawType()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
        }
        return instance;
    }

    public static <T> InternalFactory<T> fromTypeVariables(Function<?, ? extends T> supplier, Class<?> factory1Type) {
        throw new UnsupportedOperationException();
    }

}
