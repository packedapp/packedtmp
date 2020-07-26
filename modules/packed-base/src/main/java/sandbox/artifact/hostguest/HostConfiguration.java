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
package sandbox.artifact.hostguest;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;
import app.packed.component.ComponentPath;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;

/**
 *
 */
public abstract class HostConfiguration<T> implements SingletonConfiguration<T> {

    /** The configuration context. */
    protected final HostConfigurationContext context;

    //
    /**
     * Creates a new configuration
     * 
     * @param context
     *            the host configuration context
     */
    protected HostConfiguration(Class<? extends T> implementation, HostConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
    }

    @Override
    public void checkConfigurable() {}

    @Override
    public ConfigSite configSite() {
        return null;
    }

    @Override
    public Optional<Class<? extends Extension>> extension() {
        return null;
    }

    @Override
    public @Nullable String getDescription() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public TypeLiteral<T> hostType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentPath path() {
        return null;
    }

    @Override
    public SingletonConfiguration<T> setDescription(String description) {
        return null;
    }

    @Override
    public SingletonConfiguration<T> setName(String name) {
        return null;
    }
}
