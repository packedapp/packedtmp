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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.packed.bundle.AnyBundle;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleLink;
import app.packed.bundle.WiringOption;
import app.packed.container.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
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
    final Map<String, BundleLink> containers = new HashMap<>();

    /** All extensions that have been installed for the container. */
    public final IdentityHashMap<Class<? extends Extension<?>>, Extension<?>> extensions = new IdentityHashMap<>();

    /** The name of the container, or null if no name has been set. */
    @Nullable
    private String name;

    public final List<WiringOption> options;

    /**
     * @param configurationSite
     */
    protected AbstractContainerConfiguration(InternalConfigurationSite configurationSite, WiringOption... options) {
        super(configurationSite);
        this.bundle = null;
        this.options = List.of(options);
    }

    /**
     * @param configurationSite
     */
    protected AbstractContainerConfiguration(InternalConfigurationSite configurationSite, Bundle bundle, WiringOption... options) {
        super(configurationSite);
        this.bundle = requireNonNull(bundle);
        this.options = List.of(options);
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
    public AbstractContainerConfiguration setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final void setName(@Nullable String name) {
        checkConfigurable();
        this.name = checkName(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Extension<T>> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        Extension<?> e = extensions.get(extensionType);
        requireNonNull(e, extensionType + "");
        return (T) e;
    }

    @Override
    public final BundleLink wire(AnyBundle child, WiringOption... options) {
        throw new UnsupportedOperationException();
    }

    private static String checkName(String name) {
        return name;
    }

    /** A wiring option that overrides any existing container name. */
    public static class OverrideNameWiringOption extends WiringOption {

        /** The (checked) name to override with. */
        private final String name;

        /**
         * Creates a new option
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public OverrideNameWiringOption(String name) {
            this.name = checkName(name);
        }

        /** {@inheritDoc} */
        @Override
        protected void process(BundleLink link) {
            ((AbstractContainerConfiguration) link.cc()).name = name;
        }
    }
}
