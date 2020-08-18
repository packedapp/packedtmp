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
package packed.internal.service.buildtime.service;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.SingletonConfiguration;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceExtension;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedComponentDriver.SingletonComponentDriver;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionNode;

/**
 *
 */
public class PackedSingletonConfiguration<T> extends AbstractComponentConfiguration implements SingletonConfiguration<T> {

    public final ComponentNodeConfiguration node;

    private BuildEntry<T> buildEntry;

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

    @SuppressWarnings("unchecked")
    BuildEntry<T> entry() {
        if (buildEntry == null) {
            ServiceExtension e = node.container().use(ServiceExtension.class);
            ServiceExtensionNode sen = ServiceExtensionNode.fromExtension(e);
            SingletonComponentDriver scd = (SingletonComponentDriver) node.driver();
            if (scd.instance != null) {
                buildEntry = (BuildEntry<T>) sen.provider().provideInstance(node, scd.instance);
            } else {
                buildEntry = sen.provider().provideFactory(node);
            }
        }
        return buildEntry;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Key<?>> key() {
        return buildEntry == null ? Optional.empty() : Optional.of(buildEntry.key());
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
        checkConfigurable();
        return buildEntry.node.exports().export(buildEntry, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }
}
