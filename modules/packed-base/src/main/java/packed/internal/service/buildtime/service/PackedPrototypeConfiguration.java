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

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.service.ExportedServiceConfiguration;
import app.packed.service.PrototypeConfiguration;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.service.buildtime.BuildEntry;

/**
 *
 */
public final class PackedPrototypeConfiguration<T> extends AbstractComponentConfiguration implements PrototypeConfiguration<T> {

    /** The service we are exposing. */
    public final BuildEntry<T> buildEntry;

    /** The component we are exposing. */
    private final ComponentNodeConfiguration component;

    /**
     * Creates a new configuration object
     * 
     * @param buildEntry
     *            the build entry to wrap
     */
    public PackedPrototypeConfiguration(ComponentNodeConfiguration component, BuildEntry<T> buildEntry) {
        super(component);
        this.buildEntry = requireNonNull(buildEntry);
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public PackedPrototypeConfiguration<T> as(Key<? super T> key) {
        checkConfigurable();
        buildEntry.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Key<?> getKey() {
        return buildEntry.key();
    }

    /** {@inheritDoc} */
    @Override
    public PackedPrototypeConfiguration<T> setName(String name) {
        component.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExportedServiceConfiguration<T> export() {
        checkConfigurable();
        return buildEntry.node.exports().export(this, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }
}
