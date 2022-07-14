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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.bean.BeanHandler;
import app.packed.operation.OperationConfiguration;

/**
 * The implementation of {@link BeanHandler}.
 * 
 * @apiNote we could just let {@link BeanSetup} implement {@link BeanHandler}, but we choose not to, to avoid parameterizing
 *          {@link BeanSetup}.
 */
public /* primitive */ record PackedBeanHandler<T> (BeanSetup bean) implements BeanHandler<T> {

    /** {@inheritDoc} */
    @Override
    public OperationConfiguration functionalOperationBuilder(Object functionInstance) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanHandler<T> addWiringAction(Runnable action) {
        requireNonNull(action, "action is null");
        bean.wiringActions.add(action);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return bean.beanClass();
    }

    /** {@inheritDoc} */
    @Override
    public void decorateInstance(Function<? super T, ? extends T> decorator) {}

    /** {@inheritDoc} */
    @Override
    public void peekInstance(Consumer<? super T> consumer) {}

    /** {@inheritDoc} */
    @Override
    public Key<?> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("Keys are not support for void bean classes");
        }
        return Key.of(beanClass());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return !bean.realm.isClosed();
    }
}