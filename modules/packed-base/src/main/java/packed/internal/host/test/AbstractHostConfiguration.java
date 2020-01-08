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

    /** The configuration context. */
    final HostConfigurationContext context;

    /** The type of host this configuration creates. */
    // final Class<?> hostType;

    /**
     * @param context
     *            the host configuration context
     */
    protected AbstractHostConfiguration(HostConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
        // this.hostType = requireNonNull(hostType, "hostType is null");
    }

    @Override
    public final void checkConfigurable() {
        context.checkConfigurable();
    }

    @Override
    public final ConfigSite configSite() {
        return context.configSite();
    }

    @Override
    public final Optional<Class<? extends Extension>> extension() {
        return context.extension();
    }

    @Override
    public final FeatureMap features() {
        return context.features();
    }

    @Override
    @Nullable
    public final String getDescription() {
        return context.getDescription();
    }

    @Override
    public final String getName() {
        return context.getName();
    }

    @Override
    public final ComponentPath path() {
        return context.path();
    }

    @Override
    public AbstractHostConfiguration setDescription(String description) {
        context.setDescription(description);
        return this;
    }

    @Override
    public AbstractHostConfiguration setName(String name) {
        context.setName(name);
        return this;
    }

    @Override
    public final ComponentType type() {
        return ComponentType.HOST;
    }
}
