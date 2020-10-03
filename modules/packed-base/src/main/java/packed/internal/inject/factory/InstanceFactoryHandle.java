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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import app.packed.base.TypeLiteral;
import app.packed.inject.Factory;
import packed.internal.inject.dependency.DependencyDescriptor;

/** A function handle for instances, usually created with {@link Factory#ofInstance(Object)}. */
final class InstanceFactoryHandle<T> extends FactoryHandle<T> {

    /** The instance that is returned every time. */
    private final T instance;

    private InstanceFactoryHandle(TypeLiteral<T> typeLiteralOrKey, T instance, Class<?> actualType) {
        super(typeLiteralOrKey, actualType);
        this.instance = instance;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle(Lookup ignore) {
        return MethodHandles.constant(instance.getClass(), instance);
    }

    @Override
    public List<DependencyDescriptor> dependencies() {
        return List.of();
    }

    /**
     * Creates a new instance function handle from the specified instance
     *
     * @param <T>
     *            the type of instance
     * @param instance
     *            the instance
     * @return a new instance function handle
     * @see Factory#ofInstance(Object)
     */
    @SuppressWarnings("unchecked")
    static <T> FactoryHandle<T> of(T instance) {
        requireNonNull(instance, "instance is null");
        Class<?> type = instance.getClass();
        return new InstanceFactoryHandle<T>((TypeLiteral<T>) TypeLiteral.of(type), instance, type);
    }
}
