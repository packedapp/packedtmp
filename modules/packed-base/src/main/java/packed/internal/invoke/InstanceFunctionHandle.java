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
package packed.internal.invoke;

import static java.util.Objects.requireNonNull;

import app.packed.inject.Factory;
import app.packed.util.TypeLiteral;

/** A function handle that takes no arguments and returns the same instance every time. */
public final class InstanceFunctionHandle<T> extends FunctionHandle<T> {

    /** The instance that is returned every time. */
    private final T instance;

    private InstanceFunctionHandle(TypeLiteral<T> typeLiteralOrKey, T instance, Class<?> actualType) {
        super(typeLiteralOrKey, actualType);
        this.instance = instance;
    }

    /** {@inheritDoc} */
    @Override
    public T invoke(Object[] ignore) {
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
    public static <T> FunctionHandle<T> of(T instance) {
        requireNonNull(instance, "instance is null");
        Class<?> type = instance.getClass();
        return new InstanceFunctionHandle<T>((TypeLiteral<T>) TypeLiteral.of(type), instance, type);
    }
}
