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
package packed.internal.inject.invokable;

import static java.util.Objects.requireNonNull;

import app.packed.inject.TypeLiteral;

/**
 *
 */
public class InvokableInstance<T> implements Invokable<T> {

    /** The instance. */
    public final T instance;

    /** The type of objects this factory creates. */
    public final TypeLiteral<T> typeLiteral;

    @SuppressWarnings("unchecked")
    public InvokableInstance(T instance) {
        this.instance = requireNonNull(instance, "instance is null");
        this.typeLiteral = (TypeLiteral<T>) TypeLiteral.of(instance.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public TypeLiteral<T> getType() {
        return typeLiteral;
    }

    /** {@inheritDoc} */
    @Override
    public T invoke(Object[] arguments) {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFailable() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNullable() {
        return false;
    }
}
