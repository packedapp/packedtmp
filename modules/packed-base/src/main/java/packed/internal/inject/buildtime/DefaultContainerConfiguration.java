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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.AnyBundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.InstantiationMode;
import app.packed.util.Nullable;
import packed.internal.classscan.DescriptorFactory;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.AppPackedContainerSupport;
import packed.internal.container.ExtensionInfo;
import packed.internal.container.WireletList;

/** The default implementation of {@link ContainerConfiguration}. */
public class DefaultContainerConfiguration implements ContainerConfiguration {

    /** The lookup object. We default to public access */
    public DescriptorFactory accessor = DescriptorFactory.PUBLIC;

    /** The bundle we created this configuration from. Or null if we are using a configurator of some kind. */
    @Nullable
    public final AnyBundle bundle;

    /** All registered components. */
    public final LinkedHashMap<String, ComponentBuildNode> components = new LinkedHashMap<>();

    /** The configuration site of this object. */
    private final InternalConfigurationSite configurationSite;

    /** An optional description of the container . */
    @Nullable
    private String description;

    /** All extensions that are currently used. */
    final LinkedHashMap<Class<? extends Extension<?>>, Extension<?>> extensions = new LinkedHashMap<>();

    /** The name of the container, or null if no name has been set. */
    @Nullable
    private String name;

    /** The wiring options used when creating this configuration. */
    private final WireletList wirelets;

    /** All child containers, in order of wiring order. */
    final LinkedHashMap<String, DefaultContainerConfiguration> wirings = new LinkedHashMap<>();

    protected DefaultContainerConfiguration(InternalConfigurationSite configurationSite, @Nullable AnyBundle bundle, Wirelet... wirelets) {
        this.configurationSite = requireNonNull(configurationSite);
        this.bundle = bundle;
        this.wirelets = WireletList.of(wirelets);
    }

    public final void checkConfigurable() {

    }

    /** {@inheritDoc} */
    @Override
    public InternalConfigurationSite configurationSite() {
        return configurationSite;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    public void buildBundle(BundleDescriptor.Builder builder) {
        for (Extension<?> e : extensions.values()) {
            e.buildBundle(builder);
        }
    }

    public void finish() {
        for (Extension<?> e : extensions.values()) {
            e.onFinish();
        }
    }

    public final <W> void forEachWirelet(Class<W> wireletType, Consumer<? super W> action) {
        requireNonNull(wireletType, "wireletType is null");
        requireNonNull(action, "action is null");
    }

    /**
     * Returns the description.
     *
     * @return the configuration
     */
    @Override
    public final String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration install(Class<?> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration install(Factory<?> factory) {
        return new DefaultComponentConfiguration(this, factory, InstantiationMode.SINGLETON);
    }

    @Override
    public final ComponentConfiguration install(Object instance) {
        return new DefaultComponentConfiguration(this, instance);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration installStatic(Class<?> implementation) {
        return new DefaultComponentConfiguration(this, Factory.findInjectable(implementation), InstantiationMode.NONE);
    }

    @Override
    public final <T extends AnyBundle> T link(T bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        ContainerBuilder builder = new ContainerBuilder(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF), bundle, wirelets);
        builder.build();
        wirings.put(builder.getName(), builder);
        return bundle;
    }

    /** {@inheritDoc} */
    @Override
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to use public access (default)");

        this.accessor = DescriptorFactory.get(lookup);
    }

    /**
     * Sets the description of this configuration.
     *
     * @param description
     *            the description to set
     * @return this configuration
     * @throws IllegalStateException
     *             if this configuration can no longer be configured
     */
    @Override
    public DefaultContainerConfiguration setDescription(String description) {
        checkConfigurable();
        this.description = description;
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
        return (T) extensions.computeIfAbsent(extensionType, k -> {
            checkConfigurable(); // we can use extension that have already been installed, but not add new
            Extension<?> e = ExtensionInfo.newInstance(extensionType);
            AppPackedContainerSupport.invoke().setExtensionConfiguration(e, this);
            return e;
        });
    }

    /** {@inheritDoc} */
    @Override
    public List<Wirelet> wirelets() {
        return wirelets.list();
    }

    private static String checkName(String name) {
        return name;
    }

    /** A wiring option that overrides any existing container name. */
    public static class OverrideNameWiringOption extends Wirelet {

        /** The (checked) name to override with. */
        final String name;

        /**
         * Creates a new option
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public OverrideNameWiringOption(String name) {
            this.name = checkName(name);
        }

        // /** {@inheritDoc} */
        // @Override
        // protected void process(BundleLink link) {
        // ((DefaultContainerConfiguration) link.cc()).name = name;
        // }
    }
}
//
// // Maybe should be able to define a namig strategy, to avoid reuse? Mostly for distributed
// // Lazy initialized... Maybe this is part of the Specification/ContainerConfigurationProvider
// final ConcurrentHashMap<String, AtomicLong> autoGeneratedComponentNames = new ConcurrentHashMap<>();

// public void newOperation() {
// AbstractFreezableNode c = currentNode;
// if (c != null) {
// c.freeze();
// }
// currentNode = null;
// }
//
// final void checkConfigurable() {
//
// }
//
// public <T extends AbstractFreezableNode> T newOperation(T node) {
// AbstractFreezableNode c = currentNode;
// if (c != null) {
// c.freeze();
// }
// currentNode = node;
// return node;
// }