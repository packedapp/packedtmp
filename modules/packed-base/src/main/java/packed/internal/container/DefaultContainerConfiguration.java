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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.component.Install;
import app.packed.container.AnyBundle;
import app.packed.container.BuildContext;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLayer;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorExtension;
import app.packed.util.Nullable;
import packed.internal.classscan.DescriptorFactory;
import packed.internal.componentcache.ComponentClassDescriptor;
import packed.internal.componentcache.ComponentLookup;
import packed.internal.componentcache.ContainerConfiguratorCache;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.DefaultInjector;
import packed.internal.support.AppPackedContainerSupport;

/** The default implementation of {@link ContainerConfiguration}. */
// <T extends ComponentConfigurationCache>
public final class DefaultContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    /** The lookup object. We default to public access */
    public DescriptorFactory oldAccessor = DescriptorFactory.PUBLIC;

    /** The bundle we created this configuration from. Or null if we using a configurator. */
    @Nullable
    public final AnyBundle bundle;

    /** The configurator cache. */
    final ContainerConfiguratorCache ccc;

    /** All the extensions registered with this extension, ordered by first use. */
    private final LinkedHashMap<Class<? extends Extension<?>>, Extension<?>> extensions = new LinkedHashMap<>();

    private ComponentLookup lookup;

    /** A list of wirelets used when creating this configuration. */
    final WireletList wirelets;

    final InternalBuildContext buildContext;

    DefaultContainerConfiguration(@Nullable DefaultContainerConfiguration parent, @Nullable BuildContext.OutputType outputType, Class<?> configuratorType,
            @Nullable AnyBundle bundle, Wirelet... wirelets) {
        super(parent == null ? InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF)
                : parent.configurationSite().thenStack(ConfigurationSiteType.INJECTOR_OF), parent);
        this.buildContext = parent == null ? new InternalBuildContext(this, outputType) : parent.buildContext;
        this.lookup = this.ccc = ContainerConfiguratorCache.of(configuratorType);
        this.bundle = bundle;
        this.wirelets = WireletList.of(wirelets);
    }

    public InternalContainer buildContainer() {
        return new InternalContainer(null, this, buildInjector());
    }

    public void buildDescriptor(BundleDescriptor.Builder builder) {
        configure();
        finish2ndPass();
        builder.setBundleDescription(description);
        builder.setName(getName());
        for (Extension<?> e : extensions.values()) {
            e.buildBundle(builder);
        }
    }

    public Injector buildInjector() {
        configure();
        finish2ndPass();
        if (children != null) {
            for (AbstractComponentConfiguration acc : children.values()) {
                if (acc instanceof DefaultContainerConfiguration) {
                    DefaultContainerConfiguration dcc = (DefaultContainerConfiguration) acc;
                    dcc.getName();
                    dcc.buildContainer();
                }
            }
        }
        if (extensions.containsKey(InjectorExtension.class)) {
            return use(InjectorExtension.class).builder.publicInjector;
        } else {
            return new DefaultInjector(this, new ServiceNodeMap());
        }
    }

    public DefaultContainerImage buildImage() {
        configure();
        buildImage0();
        return new DefaultContainerImage(this);
    }

    private void buildImage0() {
        finish2ndPass();
        if (children != null) {
            for (AbstractComponentConfiguration acc : children.values()) {
                if (acc instanceof DefaultContainerConfiguration) {
                    DefaultContainerConfiguration dcc = (DefaultContainerConfiguration) acc;
                    dcc.buildImage0();
                }
            }
        }
    }

    InternalContainer buildFromImage() {
        return new InternalContainer(null, this, buildFromImageInjector());
    }

    private DefaultInjector buildFromImageInjector() {
        if (extensions.containsKey(InjectorExtension.class)) {
            return use(InjectorExtension.class).builder.publicInjector;
        } else {
            return new DefaultInjector(this, new ServiceNodeMap());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void link(AnyBundle bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");

        lazyInitializeName(State.LINK_INVOKED, null);
        prepareNewComponent(State.LINK_INVOKED);
        // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
        // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
        // loop situations, for example, if a bundle recursively links itself which fails by throwing
        // java.lang.StackOverflowError instead.
        DefaultContainerConfiguration dcc = new DefaultContainerConfiguration(this, null, bundle.getClass(), bundle, wirelets);
        dcc.configure();
        if (children == null) {
            children = new LinkedHashMap<>();
        }
        // Maaske skal man ikke tillade at saette navn efter man har linket....
        // Maaske skal saet navn bare vaere den foerste expression
        // Problemet er her at vi ikke vil kunne f.eks. faa hele en actor path....
        // The name of the container must be set before installing new components or linking other containers
        // And can only be set once...

        children.put(dcc.name, dcc);// name has already been verified via configure()->finalizeName()
    }

    private void configure() {
        if (hasBeenCalled) {
            return;
            // throw new IllegalStateException();
        }
        hasBeenCalled = true;
        if (bundle != null) {
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                install(bundle);
            }
            bundle.doConfigure(this);
        }
        getName();// Initializes the name
    }

    private boolean hasBeenCalled;

    public void finish2ndPass() {
        // Technically we should have a finish statement here, but dont need it
        prepareNewComponent(State.GET_NAME_INVOKED);
        for (Extension<?> e : extensions.values()) {
            e.onFinish();
        }
    }

    @Override
    public final void checkConfigurable() {

    }

    /** {@inheritDoc} */
    @Override
    public InternalConfigurationSite configurationSite() {
        return (InternalConfigurationSite) super.configurationSite();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension<?>>> extensions() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    public <W> void forEachWirelet(Class<W> wireletType, Consumer<? super W> action) {
        requireNonNull(wireletType, "wireletType is null");
        requireNonNull(action, "action is null");
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

    public ComponentConfiguration install(Class<?> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    public ComponentConfiguration install(Factory<?> factory) {
        requireNonNull(factory, "factory is null");
        ComponentClassDescriptor descriptor = lookup.componentDescriptorOf(factory.rawType());

        // All validation should be done by here..
        prepareNewComponent(State.INSTALL_INVOKED);

        DefaultComponentConfiguration dcc = current = new DefaultComponentConfiguration(configurationSite().thenStack(ConfigurationSiteType.COMPONENT_INSTALL),
                this, descriptor);
        descriptor.initialize(this, dcc);
        return dcc;

    }

    public ComponentConfiguration install(Object instance) {
        // TODO should we allow installing bundles in this way?????
        // Or any other ContainerSource... Basically link(ContainerSource) <- without wirelets....
        // I'm not sure.... Should we allow install(HelloWorldBundle.class)
        //// Nah we don't allow setting the name after we have finished...

        requireNonNull(instance, "instance is null");
        ComponentClassDescriptor descriptor = lookup.componentDescriptorOf(instance.getClass());

        // All validation should be done by here..
        prepareNewComponent(State.INSTALL_INVOKED);

        DefaultComponentConfiguration dcc = current = new FixedInstanceComponentConfiguration(
                configurationSite().thenStack(ConfigurationSiteType.COMPONENT_INSTALL), this, descriptor, instance);

        descriptor.initialize(this, dcc);
        return dcc;
    }

    private void prepareNewComponent(State state) {
        if (current != null) {
            current.onFreeze();
        }
    }

    public ComponentConfiguration installHelper(Class<?> implementation) {
        throw new UnsupportedOperationException();
        // return new OldDefaultComponentConfiguration(this, Factory.findInjectable(implementation), InstantiationMode.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? ccc : ccc.withLookup(lookup);
        this.oldAccessor = DescriptorFactory.get(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public DefaultContainerConfiguration setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DefaultContainerConfiguration setName(String name) {
        super.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Extension<T>> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return (T) extensions.computeIfAbsent(extensionType, k -> {
            checkConfigurable(); // we can use extensions that have already been installed, but not add new ones
            Extension<?> e = ExtensionClassCache.newInstance(extensionType);
            AppPackedContainerSupport.invoke().initializeExtension(e, this);
            return e;
        });
    }

    /** {@inheritDoc} */
    @Override
    public WireletList wirelets() {
        return wirelets;
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
    public static class NameWirelet extends Wirelet {

        /** The (checked) name to override with. */
        final String name;

        /**
         * Creates a new option
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public NameWirelet(String name) {
            this.name = checkName(name);
        }
    }

    private HashMap<String, DefaultLayer> layers;

    /** {@inheritDoc} */
    @Override
    public ContainerLayer newLayer(String name, ContainerLayer... dependencies) {
        HashMap<String, DefaultLayer> l = layers;
        if (l == null) {
            l = layers = new HashMap<>();
        }
        DefaultLayer newLayer = new DefaultLayer(this, name, dependencies);
        if (l.putIfAbsent(name, newLayer) != null) {
            throw new IllegalArgumentException("A layer with the name '" + name + "' has already been added");
        }
        return newLayer;
    }

    /** {@inheritDoc} */
    @Override
    public BuildContext buildContext() {
        return buildContext;
    }

}
