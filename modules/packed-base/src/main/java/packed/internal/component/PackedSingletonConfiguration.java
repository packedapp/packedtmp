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
package packed.internal.component;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.SingletonConfiguration;
import app.packed.service.ServiceComponentConfiguration;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceExtension;

/**
 *
 */
public class PackedSingletonConfiguration<T> extends AbstractComponentConfiguration implements SingletonConfiguration<T> {

    public final ComponentNodeConfiguration node;

    private ServiceComponentConfiguration<T> provide;

    public PackedSingletonConfiguration(ComponentNodeConfiguration node) {
        super(node);
        this.node = node;
    }

    /** {@inheritDoc} */
    @Override
    public SingletonConfiguration<T> as(Key<? super T> key) {
        checkConfigurable();
        entry().as(key);
        return this;
    }

    ServiceComponentConfiguration<T> entry() {
        if (provide == null) {
            ServiceExtension e = node.container.use(ServiceExtension.class);
            provide = e.provide(this);
        }
        return provide;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Key<?>> key() {
        return provide == null ? Optional.empty() : provide.key();
    }

    /** {@inheritDoc} */
    @Override
    public SingletonConfiguration<T> provide() {
        entry();
        // TODO should provide as the default key
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedSingletonConfiguration<T> setName(String name) {
        node.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> export() {
        return entry().export();
    }
}
