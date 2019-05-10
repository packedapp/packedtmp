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
package packed.internal.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import app.packed.bundle.Bundle;
import app.packed.bundle.WiredBundle;
import app.packed.bundle.WiringOperation;
import app.packed.container.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.extension.AnyBundle;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
import packed.internal.classscan.DescriptorFactory;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.util.AbstractConfiguration;

/**
 *
 */
public abstract class AbstractContainerConfiguration extends AbstractConfiguration implements ContainerConfiguration {

    /** The lookup object. We default to public access */
    public DescriptorFactory accessor = DescriptorFactory.PUBLIC;

    /**
     * The (optional) bundle we are creating a runtime instance for, if null then we using a {@code XConfiguration} to
     * create the runtime.
     */
    @Nullable
    public final Bundle bundle;

    /** All outgoing links of this container, in order of installation order. */
    final Map<String, WiredBundle> containers = new HashMap<>();

    /** All extensions that have been installed for the container. */
    public final IdentityHashMap<Class<? extends Extension<?>>, Extension<?>> extensions = new IdentityHashMap<>();

    /** The name of the container, or null if no name has been set. */
    @Nullable
    private String name;

    /**
     * @param configurationSite
     */
    protected AbstractContainerConfiguration(InternalConfigurationSite configurationSite) {
        super(configurationSite);
        this.bundle = null;
    }

    /**
     * @param configurationSite
     */
    protected AbstractContainerConfiguration(InternalConfigurationSite configurationSite, Bundle bundle) {
        super(configurationSite);
        this.bundle = requireNonNull(bundle);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final ComponentConfiguration install(Object instance) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to use public access (default)");
        checkConfigurable();
        this.accessor = DescriptorFactory.get(lookup);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Extension<T>> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        Extension<?> e = extensions.get(extensionType);
        requireNonNull(e, extensionType + "");
        return (T) e;
    }

    /** {@inheritDoc} */
    @Override
    public final void setName(@Nullable String name) {
        checkConfigurable();
        this.name = name;
    }

    @Override
    public final WiredBundle wire(AnyBundle child, WiringOperation... options) {
        throw new UnsupportedOperationException();
    }
}
