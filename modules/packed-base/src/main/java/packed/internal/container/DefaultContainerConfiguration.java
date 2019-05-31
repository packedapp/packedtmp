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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.component.Install;
import app.packed.container.AnyBundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.Container;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorExtension;
import app.packed.inject.InstantiationMode;
import app.packed.util.Nullable;
import packed.internal.classscan.DescriptorFactory;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.buildtime.DefaultComponentConfiguration;
import packed.internal.inject.buildtime.DependencyGraph;

/** The default implementation of {@link ContainerConfiguration}. */
public final class DefaultContainerConfiguration implements ContainerConfiguration {

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

    private boolean isConfigured;

    /** The name of the container, or null if no name has been set. */
    @Nullable
    private String name;

    final DefaultContainerConfiguration parent;

    /** A list of wirelets used when creating this configuration. */
    private final WireletList wirelets;

    /** All child containers, in order of wiring order. */
    final LinkedHashMap<String, DefaultContainerConfiguration> wirings = new LinkedHashMap<>();

    public DefaultContainerConfiguration(ContainerType type, @Nullable AnyBundle bundle, Wirelet... wirelets) {
        this.configurationSite = InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF);
        this.bundle = bundle;
        this.parent = null;
        this.wirelets = WireletList.of(wirelets);
    }

    public DefaultContainerConfiguration(DefaultContainerConfiguration parent, ContainerType type, @Nullable AnyBundle bundle, Wirelet... wirelets) {
        this.configurationSite = InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF);
        this.bundle = bundle;
        this.parent = requireNonNull(parent);
        this.wirelets = WireletList.of(wirelets);
    }

    public Container build() {
        configure();
        return new InternalContainer(this, buildInjector());
    }

    public void createDescriptor(BundleDescriptor.Builder builder) {
        configure();
        finish();

        DependencyGraph injectorBuilder = new DependencyGraph(this);
        injectorBuilder.analyze();

        for (Extension<?> e : extensions.values()) {
            e.buildBundle(builder);
        }
    }

    public Injector buildInjector() {
        finish();
        new DependencyGraph(this).instantiate();
        return use(InjectorExtension.class).ib.publicInjector;
    }

    public final void checkConfigurable() {

    }

    /** {@inheritDoc} */
    @Override
    public InternalConfigurationSite configurationSite() {
        return configurationSite;
    }

    public void configure() {
        if (!isConfigured) {
            if (bundle != null) {
                if (bundle.getClass().isAnnotationPresent(Install.class)) {
                    install(bundle);
                }
                bundle.doConfigure(this);
            }
        }
        isConfigured = true;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    public void finish() {
        for (Extension<?> e : extensions.values()) {
            e.onFinish();
        }
    }

    public <W> void forEachWirelet(Class<W> wireletType, Consumer<? super W> action) {
        requireNonNull(wireletType, "wireletType is null");
        requireNonNull(action, "action is null");
    }

    /**
     * Returns the description.
     *
     * @return the configuration
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns an extension of the specified type if installed, otherwise null.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type if installed, otherwise null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Extension<T>> T getExtension(Class<T> extensionType) {
        return (T) extensions.get(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
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

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration install(Object instance) {
        return new DefaultComponentConfiguration(this, instance);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration installStatic(Class<?> implementation) {
        return new DefaultComponentConfiguration(this, Factory.findInjectable(implementation), InstantiationMode.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends AnyBundle> T link(T bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(this, ContainerType.LINK, bundle, wirelets);
        // Implementation node, we can do linking (calling bundle.configure) in two ways. Immediately or later after the parent
        // has been fully configured. We choose immediately because we get nice stack traces.
        configuration.configure();

        // TODO what about name
        wirings.put(configuration.getName(), configuration);
        return bundle;
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(Lookup lookup) {
        // Actually I think null might be okay, then its standard module-info.java

        // Component X has access to G, but Packed does not have access

        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to use public access (default)");
        this.accessor = DescriptorFactory.get(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public DefaultContainerConfiguration setDescription(String description) {
        checkConfigurable();
        this.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@Nullable String name) {
        checkConfigurable();
        this.name = checkName(name);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Extension<T>> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return (T) extensions.computeIfAbsent(extensionType, k -> {
            checkConfigurable(); // we can use extensions that have already been installed, but not add new ones
            return ExtensionInfo.newInstance(this, extensionType);
        });
    }

    /** {@inheritDoc} */
    @Override
    public List<Wirelet> wirelets() {
        return wirelets.list();
    }

    /**
     * Checks the name of the container or component.
     * 
     * @param name
     *            the name to check
     * @return the name if valid
     */
    private static String checkName(String name) {
        if (name != null) {

        }
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