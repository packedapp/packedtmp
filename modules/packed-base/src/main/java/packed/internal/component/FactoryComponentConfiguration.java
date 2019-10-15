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

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.config.ConfigSite;
import app.packed.service.Dependency;
import app.packed.service.Factory;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.Instantiable;

/**
 *
 */
public final class FactoryComponentConfiguration<T> extends CoreComponentConfiguration<T> implements Instantiable {

    public final Factory<T> factory;

    /**
     * @param configSite
     * @param containerConfiguration
     * @param model
     */
    public FactoryComponentConfiguration(ConfigSite configSite, PackedContainerConfiguration containerConfiguration, ComponentModel model, Factory<T> factory) {
        super(configSite, containerConfiguration, model);
        this.factory = requireNonNull(factory);
    }

    /** {@inheritDoc} */
    @Override
    public List<Dependency> dependencies() {
        throw new UnsupportedOperationException();
    }
}
