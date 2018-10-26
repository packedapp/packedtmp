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
import java.util.function.Supplier;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.InjectionException;
import app.packed.inject.TypeLiteral;
import app.packed.inject.TypeLiteralOrKey;

/** An internal factory for {@link Factory0}. */
public final class InternalFactory0<T> extends InternalFactory<T> {

    /** A cache of the type literal of classes extending {@link Factory0}. */
    private static final ClassValue<TypeLiteral<?>> TYPE_PARAMETER = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected TypeLiteral<?> computeValue(Class<?> type) {
            return TypeLiteral.fromTypeVariable(Factory.class, 0, (Class) type);
        }
    };

    /** The supplier that creates the actual objects. */
    private final Supplier<? extends T> supplier;

    /**
     * @param typeLiteralOrKey
     * @param dependencies
     */
    public InternalFactory0(Supplier<? extends T> supplier, TypeLiteralOrKey<T> typeLiteralOrKey) {
        super(typeLiteralOrKey);
        this.supplier = requireNonNull(supplier, "supplier is null");
    }

    @SuppressWarnings("unchecked")
    public static <T> InternalFactory<T> of(Supplier<? extends T> supplier, Class<?> factory0Type) {
        return new InternalFactory0<>(supplier, (TypeLiteral<T>) TYPE_PARAMETER.get(factory0Type));
    }

    @Override
    public Class<?> getLowerBound() {
        return Object.class; //The raw supplier generate objects
    }

    static class CachedFactoryDefinition {
        final List<Dependency> dependencies;

        final TypeLiteral<?> objectType;

        CachedFactoryDefinition(TypeLiteral<?> objectType, List<Dependency> dependencies) {
            this.objectType = requireNonNull(objectType);
            this.dependencies = requireNonNull(dependencies);
        }
    }

    /** {@inheritDoc} */
    @Override
    public T instantiate(Object[] ignore) {
        T instance = supplier.get();
        if (!getRawType().isInstance(instance)) {
            throw new InjectionException(
                    "The Supplier '" + format(supplier.getClass()) + "' used when creating a Factory0 instance was expected to produce instances of '"
                            + format(getRawType()) + "', but it created an instance of '" + format(instance.getClass()) + "'");
        }
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public List<Dependency> getDependencies() {
        return List.of();
    }
}
