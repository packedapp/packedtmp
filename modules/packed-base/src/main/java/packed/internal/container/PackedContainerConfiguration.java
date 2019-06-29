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

import app.packed.component.ComponentConfiguration;
import app.packed.component.Install;
import app.packed.container.ArtifactBuildContext;
import app.packed.container.ArtifactType;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerBundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLayer;
import app.packed.container.Extension;
import app.packed.container.InstantiationContext;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.inject.Factory;
import app.packed.inject.InjectorExtension;
import app.packed.util.Nullable;
import packed.internal.classscan.DescriptorFactory;
import packed.internal.componentcache.ComponentClassDescriptor;
import packed.internal.componentcache.ComponentLookup;
import packed.internal.componentcache.ContainerConfiguratorCache;
import packed.internal.config.site.ConfigSiteType;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.DefaultInjector;
import packed.internal.support.AppPackedContainerSupport;

/** The default implementation of {@link ContainerConfiguration}. */
// <T extends ComponentConfigurationCache>
public final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    /** The configurator cache. */
    final ContainerConfiguratorCache ccc;

    /** All the extensions registered with this extension, ordered by first use. */
    private final LinkedHashMap<Class<? extends Extension<?>>, Extension<?>> extensions = new LinkedHashMap<>();

    private HashMap<String, DefaultLayer> layers;

    private ComponentLookup lookup;

    /** The lookup object. We default to public access */
    public DescriptorFactory oldAccessor = DescriptorFactory.PUBLIC;

    public final InternalContainerSource source;

    /** Any wirelets that was used when creating this configuration. */
    private final WireletList wirelets;

    public PackedContainerConfiguration(ArtifactType artifactType, InternalContainerSource source, Wirelet... wirelets) {
        super(InternalConfigSite.ofStack(ConfigSiteType.INJECTOR_OF), artifactType);
        this.source = requireNonNull(source);
        this.lookup = this.ccc = source.cache();
        this.wirelets = WireletList.of(wirelets);
    }

    public PackedContainerConfiguration(PackedContainerConfiguration parent, InternalContainerSource source, Wirelet... wirelets) {
        super(parent.configSite().thenStack(ConfigSiteType.INJECTOR_OF), parent);
        this.source = requireNonNull(source);
        this.lookup = this.ccc = source.cache();
        this.wirelets = WireletList.of(wirelets);
    }

    public PackedContainerConfiguration build() {
        configure();
        extensionsContainerConfigured();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactBuildContext buildContext() {
        return buildContext;
    }

    public void buildDescriptor(BundleDescriptor.Builder builder) {
        build();
        builder.setBundleDescription(getDescription());
        builder.setName(getName());
        for (Extension<?> e : extensions.values()) {
            e.buildBundle(builder);
        }
    }

    public DefaultInjector buildInjector() {
        build();
        new PackedContainer(null, this, new PackedInstantiationContext());
        if (extensions.containsKey(InjectorExtension.class)) {
            return use(InjectorExtension.class).builder.publicInjector;
        } else {
            return new DefaultInjector(this, new ServiceNodeMap());
        }
    }

    /**
     * Configures the configuration.
     */
    private void configure() {
        if (source.source instanceof ContainerBundle) {
            ContainerBundle bundle = (ContainerBundle) source.source;
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                install(bundle);
            }
            AppPackedContainerSupport.invoke().doConfigure(bundle, this);
        }
        // Initializes the name of the container, and sets the state to State.FINAL
        initializeName(State.FINAL, null);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension<?>>> extensions() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    final void extensionsContainerConfigured() {
        prepareNewComponent(State.GET_NAME_INVOKED);
        for (Extension<?> e : extensions.values()) {
            e.onContainerConfigured(); // State final????
        }
        if (children != null) {
            for (AbstractComponentConfiguration acc : children.values()) {
                if (acc instanceof PackedContainerConfiguration) {
                    PackedContainerConfiguration dcc = (PackedContainerConfiguration) acc;
                    dcc.extensionsContainerConfigured();
                }
            }
        }
    }

    @Override
    void extensionsPrepareInstantiation(InstantiationContext ic) {
        for (Extension<?> e : extensions.values()) {
            e.onPrepareContainerInstantiate(ic);
        }
        super.extensionsPrepareInstantiation(ic);
    }

    public ComponentConfiguration install(Class<?> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    public ComponentConfiguration install(Factory<?> factory) {
        requireNonNull(factory, "factory is null");
        ComponentClassDescriptor descriptor = lookup.componentDescriptorOf(factory.rawType());

        // All validation should be done by here..
        prepareNewComponent(State.INSTALL_INVOKED);

        DefaultComponentConfiguration dcc = currentComponent = new DefaultComponentConfiguration(configSite().thenStack(ConfigSiteType.COMPONENT_INSTALL), this,
                descriptor);
        descriptor.initialize(this, dcc);
        return dcc;

    }

    public ComponentConfiguration install(Object instance) {
        // TODO we should allow Class instances, TypeVariable, Factory, und so weither.... Eller ogsaa skal kalde den rette
        // metode...
        // Eller maaske have installInstance();
        // TODO should we allow installing bundles in this way?????
        // Or any other ContainerSource... Basically link(ContainerSource) <- without wirelets....
        // I'm not sure.... Should we allow install(HelloWorldBundle.class)
        //// Nah we don't allow setting the name after we have finished...

        requireNonNull(instance, "instance is null");
        ComponentClassDescriptor descriptor = lookup.componentDescriptorOf(instance.getClass());

        // All validation should be done by here..
        prepareNewComponent(State.INSTALL_INVOKED);

        DefaultComponentConfiguration dcc = currentComponent = new InstantiatedComponentConfiguration(configSite().thenStack(ConfigSiteType.COMPONENT_INSTALL),
                this, descriptor, instance);

        descriptor.initialize(this, dcc);
        return dcc;
    }

    public ComponentConfiguration installHelper(Class<?> implementation) {
        requireNonNull(implementation, "implementation is null");
        prepareNewComponent(State.INSTALL_INVOKED);

        ComponentClassDescriptor descriptor = lookup.componentDescriptorOf(implementation);
        DefaultComponentConfiguration dcc = currentComponent = new StaticComponentConfiguration(configSite().thenStack(ConfigSiteType.COMPONENT_INSTALL), this,
                descriptor, implementation);
        return descriptor.initialize(this, dcc);
    }

    public PackedContainer instantiate() {
        // TODO support instantiation wirelets...
        PackedInstantiationContext pic = new PackedInstantiationContext();
        extensionsPrepareInstantiation(pic);

        // Will instantiate the whole container hierachy
        PackedContainer pc = new PackedContainer(null, this, pic);
        methodHandlePassing0(pc, pic);
        return pc;
    }

    /** {@inheritDoc} */
    @Override
    PackedContainer instantiate(AbstractComponent parent, InstantiationContext ic) {
        return new PackedContainer(parent, this, ic);

    }

    /** {@inheritDoc} */
    @Override
    public void link(ContainerBundle bundle, Wirelet... wirelets) {
        // Previously this method returned the specified bundle. However, to encourage people to configure the bundle before
        // calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have void return
        // type.

        requireNonNull(bundle, "bundle is null");
        initializeName(State.LINK_INVOKED, null);
        prepareNewComponent(State.LINK_INVOKED);

        // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
        // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
        // loop situations, for example, if a bundle recursively links itself which fails by throwing
        // java.lang.StackOverflowError instead of an infinite loop.
        PackedContainerConfiguration dcc = new PackedContainerConfiguration(this, InternalContainerSource.of(bundle), wirelets);
        dcc.configure();
        addChild(dcc);
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? ccc : ccc.withLookup(lookup);
        this.oldAccessor = DescriptorFactory.get(lookup);
    }

    private void methodHandlePassing0(AbstractComponent ac, InstantiationContext ic) {
        if (children != null) {
            for (AbstractComponentConfiguration a : children.values()) {
                AbstractComponent child = ac.children.get(a.name);
                if (a instanceof PackedContainerConfiguration) {
                    ((PackedContainerConfiguration) a).methodHandlePassing0(child, ic);
                } else {
                    DefaultComponentConfiguration dcc = (DefaultComponentConfiguration) a;
                    dcc.ccd.process(this, ic);
                }
            }
        }
    }

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

    private void prepareNewComponent(State state) {
        if (currentComponent != null) {
            currentComponent.initializeName(state, null);
            requireNonNull(currentComponent.name);
            addChild(currentComponent);
        } else {
            initializeName(State.INSTALL_INVOKED, null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerConfiguration setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerConfiguration setName(String name) {
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
}
//
/// **
// * Returns an extension of the specified type if installed, otherwise null.
// *
// * @param <T>
// * the type of extension to return
// * @param extensionType
// * the type of extension to return
// * @return an extension of the specified type if installed, otherwise null
// */
// @SuppressWarnings("unchecked")
// @Nullable
// public <T extends Extension<T>> T getExtension(Class<T> extensionType) {
// return (T) extensions.get(extensionType);
// }