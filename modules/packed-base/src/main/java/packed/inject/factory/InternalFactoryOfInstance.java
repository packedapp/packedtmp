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

import java.util.List;

import app.packed.inject.Dependency;
import app.packed.inject.Factory;
import app.packed.inject.Key;
import app.packed.inject.TypeLiteralOrKey;

/** A factory that returns the same instance on every invocation. */
public final class InternalFactoryOfInstance<T> extends InternalFactory<T> {

    /** The instance that is returned every time. */
    private final T instance;

    private InternalFactoryOfInstance(TypeLiteralOrKey<T> typeLiteralOrKey, T instance, Class<?> actualType) {
        super(typeLiteralOrKey, actualType);
        this.instance = instance;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Class<T> forScanning() {
        return (Class<T>) instance.getClass();
    }

    /** {@inheritDoc} */
    @Override
    public List<Dependency> getDependencies() {
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public T instantiate(Object[] ignore) {
        return instance;
    }

    /**
     * Creates a new instance factory from the specified instance
     *
     * @param <T>
     *            the type of instance
     * @param instance
     *            the instance
     * @return a new instance factory
     * @see Factory#ofInstance(Object)
     */
    @SuppressWarnings("unchecked")
    public static <T> InternalFactory<T> of(T instance) {
        requireNonNull(instance, "instance is null");
        Class<?> type = instance.getClass();
        return new InternalFactoryOfInstance<T>((Key<T>) Key.of(type), instance, type);
    }

    /**
     * Creates a new instance factory from the specified instance.
     *
     * @param <T>
     *            the type of instance
     * @param instance
     *            the instance
     * @param type
     *            the default type to expose the factory as
     * @return a new instance factory
     * @see Factory#ofInstance(Object)
     */
    public static <T> InternalFactory<T> of(T instance, Class<T> type) {
        requireNonNull(instance, "instance is null");
        requireNonNull(type, "type is null");
        // TODO validate Class<T>, when we write a test
        return new InternalFactoryOfInstance<T>(Key.of(type), instance, instance.getClass());
    }

    /**
     * Creates a new instance factory from the specified instance.
     *
     * @param <T>
     *            the type of instance
     * @param instance
     *            the instance
     * @param type
     *            the default type or key to expose the factory as
     * @return a new instance factory
     * @see Factory#ofInstance(Object)
     */
    public static <T> InternalFactory<T> of(T instance, TypeLiteralOrKey<T> typeLiteralOrKey) {
        requireNonNull(instance, "instance is null");
        requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
        return new InternalFactoryOfInstance<T>(typeLiteralOrKey, instance, instance.getClass());
    }
}
