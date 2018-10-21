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
package packed.inject.factory;

import static java.util.Objects.requireNonNull;
import static packed.util.Formatter.format;

import java.util.List;
import java.util.function.BiFunction;

import app.packed.inject.Dependency;
import app.packed.inject.InjectionException;
import app.packed.inject.TypeLiteral;

/**
 *
 */
public class InternalFactory2<T, U, R> extends InternalFactory<R> {

    private final List<Dependency> dependencies;

    /** The function that creates the actual objects. */
    private final BiFunction<? super T, ? super U, ? extends R> function;

    /**
     * @param factory
     * @param typeLiteral
     * @param dependencies
     */
    public InternalFactory2(BiFunction<? super T, ? super U, ? extends R> function, TypeLiteral<R> typeLiteral, List<Dependency> dependencies) {
        super(typeLiteral);
        this.function = requireNonNull(function);
        this.dependencies = requireNonNull(dependencies);
    }

    /** {@inheritDoc} */
    @Override
    public Class<R> forScanning() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public R instantiate(Object[] params) {
        T t = (T) params[0];
        U u = (U) params[1];
        R instance = function.apply(t, u);
        if (!getRawType().isInstance(instance)) {
            throw new InjectionException(
                    "The BiFunction '" + format(function.getClass()) + "' used when creating a Factory2 instance was expected to produce instances of '"
                            + format(getRawType()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
        }
        return instance;
    }

    public static <T> InternalFactory<T> of(BiFunction<?, ?, ? extends T> supplier, Class<?> factory2Type) {
        throw new UnsupportedOperationException();
    }
}
