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
package packed.internal.host.api;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentType;
import app.packed.component.FeatureMap;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;

/**
 *
 */
public abstract class AbstractHostConfiguration {

    /** The configuration context. */
    protected final HostConfigurationContext context;

    /**
     * Creates a new configuration
     * 
     * @param context
     *            the host configuration context
     */
    protected AbstractHostConfiguration(HostConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
    }

    public final void checkConfigurable() {
        context.checkConfigurable();
    }

    public final ConfigSite configSite() {
        return context.configSite();
    }

    public final Optional<Class<? extends Extension>> extension() {
        return context.extension();
    }

    public final FeatureMap features() {
        return context.features();
    }

    @Nullable
    public final String getDescription() {
        return context.getDescription();
    }

    public final String getName() {
        return context.getName();
    }

    public final ComponentPath path() {
        return context.path();
    }

    public AbstractHostConfiguration setDescription(String description) {
        context.setDescription(description);
        return this;
    }

    public AbstractHostConfiguration setName(String name) {
        context.setName(name);
        return this;
    }

    public final ComponentType type() {
        return ComponentType.HOST;
    }
}
