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
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;

/**
 * Implementation of {@link BeanHandle}.
 * 
 * @apiNote we could let {@link BeanSetup} implement {@link BeanHandle}, but we choose not to, to avoid parameterizing
 *          {@link BeanSetup}.
 */
public /* primitive */ record PackedBeanHandle<T> (BeanSetup bean) implements BeanHandle<T> {

    /** {@inheritDoc} */
    @Override
    public PackedBeanHandle<T> onWireRun(Runnable action) {
        requireNonNull(action, "action is null");
        Runnable w = bean.onWiringAction;
        if (w == null) {
            bean.onWiringAction = action;
        } else {
            bean.onWiringAction = () -> {
                w.run();
                action.run();
            };
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return bean.beanClass();
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind beanKind() {
        return bean.props.kind();
    }

    /** {@inheritDoc} */
    @Override
    public void decorateInstance(Function<? super T, ? extends T> decorator) {}

    /** {@inheritDoc} */
    @Override
    public void peekInstance(Consumer<? super T> consumer) {
        // check sourceKind!=INSTANCE
    }

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

    /** {@inheritDoc} */
    @Override
    public void specializeMirror(Supplier<? extends BeanMirror> mirrorFactory) {
        throw new UnsupportedOperationException();
    }

}
