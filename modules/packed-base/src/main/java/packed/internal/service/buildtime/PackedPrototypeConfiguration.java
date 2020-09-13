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
package packed.internal.service.buildtime;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.service.ExportedServiceConfiguration;
import app.packed.service.PrototypeConfiguration;

/**
 *
 */
public final class PackedPrototypeConfiguration<T> extends AbstractComponentConfiguration implements PrototypeConfiguration<T> {

    /**
     * Creates a new configuration object
     */
    public PackedPrototypeConfiguration(ComponentConfigurationContext component) {
        super(component);
        context.sourceProvide();
    }

    /** {@inheritDoc} */
    @Override
    public PackedPrototypeConfiguration<T> as(Key<? super T> key) {
        context.sourceProvideAs(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Key<?> getKey() {
        return context.sourceProvideAsKey().get();
    }

    /** {@inheritDoc} */
    @Override
    public PackedPrototypeConfiguration<T> setName(String name) {
        context.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExportedServiceConfiguration<T> export() {
        return context.sourceExport();
    }
}
