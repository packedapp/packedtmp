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
package packed.internal.host.test;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.artifact.HostConfiguration;
import app.packed.artifact.HostConfigurationContext;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentType;
import app.packed.component.feature.FeatureMap;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.lang.Nullable;

/**
 *
 */
public abstract class AbstractHostConfiguration implements HostConfiguration {

    protected final HostConfigurationContext wrapper;

    protected AbstractHostConfiguration(HostConfigurationContext wrapper) {
        this.wrapper = requireNonNull(wrapper);
    }

    @Override
    public final void checkConfigurable() {
        wrapper.checkConfigurable();
    }

    @Override
    public final ConfigSite configSite() {
        return wrapper.configSite();
    }

    @Override
    public final Optional<Class<? extends Extension>> extension() {
        return wrapper.extension();
    }

    @Override
    public final FeatureMap features() {
        return wrapper.features();
    }

    @Override
    @Nullable
    public final String getDescription() {
        return wrapper.getDescription();
    }

    @Override
    public final String getName() {
        return wrapper.getName();
    }

    @Override
    public final ComponentPath path() {
        return wrapper.path();
    }

    @Override
    public AbstractHostConfiguration setDescription(String description) {
        wrapper.setDescription(description);
        return this;
    }

    @Override
    public AbstractHostConfiguration setName(String name) {
        wrapper.setName(name);
        return this;
    }

    @Override
    public final ComponentType type() {
        return ComponentType.HOST;
    }
}
