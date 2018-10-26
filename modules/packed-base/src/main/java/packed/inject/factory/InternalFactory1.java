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
import java.util.function.Function;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.Factory1;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import app.packed.inject.TypeLiteral;

/** An internal factory for {@link Factory1}. */
public class InternalFactory1<T, R> extends InternalFactory<R> {

    /** A cache of function factory definitions. */
    static final ClassValue<CachedFactoryDefinition> TYPE_PARAMETERS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected CachedFactoryDefinition computeValue(Class<?> type) {
            TypeLiteral<?> tl = TypeLiteral.fromTypeVariable(Factory.class, 0, (Class) type);
            Key<?> dep = Key.getKeyOfArgument(Factory1.class, 0, (Class) getClass());
            return new CachedFactoryDefinition(tl, List.of(new Dependency(dep, null, 0)));
        }
    };

    private final List<Dependency> dependencies;

    /** The function that creates the actual objects. */
    private final Function<? super T, ? extends R> function;

    /**
     * @param factory
     * @param typeLiteral
     * @param dependencies
     */
    public InternalFactory1(Function<? super T, ? extends R> function, TypeLiteral<R> typeLiteral, List<Dependency> dependencies) {
        super(typeLiteral);
        this.function = requireNonNull(function);
        this.dependencies = requireNonNull(dependencies);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getLowerBound() {
        return Object.class; // The raw function return objects
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @SuppressWarnings("unchecked")
    @Override
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

    public static <T> InternalFactory<T> of(Function<?, ? extends T> supplier, Class<?> factory1Type) {
        throw new UnsupportedOperationException();
    }

    static class CachedFactoryDefinition {
        final List<Dependency> dependencies;

        final TypeLiteral<?> objectType;

        CachedFactoryDefinition(TypeLiteral<?> objectType, List<Dependency> dependencies) {
            this.objectType = requireNonNull(objectType);
            this.dependencies = requireNonNull(dependencies);
        }
    }
}
