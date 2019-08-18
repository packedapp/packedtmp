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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.BiConsumer;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.artifact.ArtifactSource;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Install;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLayer;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;
import app.packed.inject.Factory;
import app.packed.util.Nullable;
import packed.internal.config.site.ConfigSiteType;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.container.extension.hook.DelayedAccessor;
import packed.internal.container.extension.hook.DelayedAccessor.AbstractDelayerAccessor;
import packed.internal.container.extension.hook.DelayedAccessor.SidecarFieldDelayerAccessor;
import packed.internal.container.extension.hook.DelayedAccessor.SidecarMethodDelayerAccessor;
import packed.internal.container.model.ComponentLookup;
import packed.internal.container.model.ComponentModel;
import packed.internal.container.model.ContainerModel;
import packed.internal.support.AppPackedContainerSupport;
import packed.internal.support.AppPackedExtensionSupport;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    /** The source of the container configuration. */
    final ArtifactSource source;

    /** A configurator cache object, shared among container sources of the same type. */
    private final ContainerModel configuratorCache;

    /** All registered extensions, in order of registration. */
    private final LinkedHashMap<Class<? extends Extension>, Extension> extensions = new LinkedHashMap<>();

    private HashMap<String, DefaultLayer> layers;

    /** A lookup object. shared among friendly people. */
    public ComponentLookup lookup;

    /** Any wirelets that was given by the user when creating this configuration. */
    private final WireletList wirelets;

    /**
     * Creates a new container configuration.
     * 
     * @param artifactDriver
     *            the type of artifact driver used for creating the artifact
     * @param source
     *            the source of the container
     * @param wirelets
     *            any wirelets that was given by the user
     */
    public PackedContainerConfiguration(ArtifactDriver<?> artifactDriver, ArtifactSource source, Wirelet... wirelets) {
        super(InternalConfigSite.ofStack(ConfigSiteType.INJECTOR_OF), artifactDriver);
        this.source = requireNonNull(source);
        this.lookup = this.configuratorCache = ContainerModel.of(source.getClass());
        this.wirelets = WireletList.of(wirelets);
    }

    private PackedContainerConfiguration(PackedContainerConfiguration parent, ArtifactSource source, Wirelet... wirelets) {
        super(parent.configSite().thenStack(ConfigSiteType.INJECTOR_OF), parent);
        this.source = requireNonNull(source);
        this.lookup = this.configuratorCache = ContainerModel.of(source.getClass());
        this.wirelets = WireletList.of(wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactBuildContext buildContext() {
        return buildContext;
    }

    public void buildDescriptor(BundleDescriptor.Builder builder) {
        doBuild();
        builder.setBundleDescription(getDescription());
        builder.setName(getName());
        for (Extension e : extensions.values()) {
            AppPackedExtensionSupport.invoke().buildBundle(e, builder);
        }
    }

    // public DefaultInjector buildInjector() {
    // doBuild();
    // new PackedArtifactContext(null, this, new PackedArtifactInstantiationContext(wirelets));
    // if (extensions.containsKey(InjectionExtension.class)) {
    // return use(InjectionExtension.class).builder.publicInjector;
    // } else {
    // return new DefaultInjector(this, new ServiceNodeMap());
    // }
    // }

    /**
     * Configures the configuration.
     */
    private void configure() {
        if (source instanceof Bundle) {
            Bundle bundle = (Bundle) source;
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                install(bundle);
            }
            AppPackedContainerSupport.invoke().doConfigure(bundle, this);
        }
        // Initializes the name of the container, and sets the state to State.FINAL
        initializeName(State.FINAL, null);
        super.state = State.FINAL; // Thing is here, that initialize name returns early if name!=null
    }

    public PackedContainerConfiguration doBuild() {
        configure();
        extensionsContainerConfigured();
        return this;
    }

    public PackedArtifactContext doInstantiate(WireletList additionalWirelets) {
        // TODO support instantiation wirelets for images
        PackedArtifactInstantiationContext pic = new PackedArtifactInstantiationContext(wirelets.plus(additionalWirelets));
        extensionsPrepareInstantiation(pic);

        // Will instantiate the whole container hierachy
        PackedArtifactContext pc = new PackedArtifactContext(null, this, pic);
        methodHandlePassing0(pc, pic);
        return pc;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> extensions() {
        // TODO should we contract wise say that we return them in order of usage???
        // Topologically sorted??? If we keep track of this at runtime I think we should
        return Collections.unmodifiableSet(extensions.keySet());
    }

    void extensionsContainerConfigured() {
        prepareNewComponent(State.GET_NAME_INVOKED);
        for (Extension e : extensions.values()) {
            AppPackedExtensionSupport.invoke().onConfigured(e);
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
    void extensionsPrepareInstantiation(ArtifactInstantiationContext ic) {
        for (Extension e : extensions.values()) {
            AppPackedExtensionSupport.invoke().onPrepareContainerInstantiation(e, ic);
        }
        super.extensionsPrepareInstantiation(ic);
    }

    public ComponentConfiguration install(Class<?> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    public ComponentConfiguration install(Factory<?> factory) {
        // We actually do not need to create a method handle if we are just creating a descriptor...
        // On the other hand, validation is nice right???
        requireNonNull(factory, "factory is null");
        ComponentModel descriptor = lookup.componentModelOf(factory.rawType());

        // All validation should be done by here..
        prepareNewComponent(State.INSTALL_INVOKED);

        DefaultComponentConfiguration dcc = currentComponent = new FactoryComponentConfiguration(configSite().thenStack(ConfigSiteType.COMPONENT_INSTALL), this,
                descriptor, factory);
        return descriptor.initialize(this, dcc);
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
        ComponentModel descriptor = lookup.componentModelOf(instance.getClass());

        // All validation should be done by here..
        prepareNewComponent(State.INSTALL_INVOKED);

        DefaultComponentConfiguration dcc = currentComponent = new InstantiatedComponentConfiguration(configSite().thenStack(ConfigSiteType.COMPONENT_INSTALL),
                this, descriptor, instance);

        return descriptor.initialize(this, dcc);
    }

    public ComponentConfiguration installHelper(Class<?> implementation) {
        requireNonNull(implementation, "implementation is null");
        prepareNewComponent(State.INSTALL_INVOKED);

        ComponentModel descriptor = lookup.componentModelOf(implementation);
        DefaultComponentConfiguration dcc = currentComponent = new StaticComponentConfiguration(configSite().thenStack(ConfigSiteType.COMPONENT_INSTALL), this,
                descriptor, implementation);
        return descriptor.initialize(this, dcc);
    }

    /** {@inheritDoc} */
    @Override
    PackedArtifactContext instantiate(AbstractComponent parent, ArtifactInstantiationContext ic) {
        return new PackedArtifactContext(parent, this, ic);
    }

    public void link(Bundle bundle, Wirelet... wirelets) {
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
        PackedContainerConfiguration dcc = new PackedContainerConfiguration(this, bundle, wirelets);
        dcc.configure();
        addChild(dcc);
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? configuratorCache : configuratorCache.withLookup(lookup);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void methodHandlePassing0(AbstractComponent ac, ArtifactInstantiationContext ic) {
        if (children != null) {
            for (AbstractComponentConfiguration a : children.values()) {
                AbstractComponent child = ac.children.get(a.name);
                if (a instanceof PackedContainerConfiguration) {
                    ((PackedContainerConfiguration) a).methodHandlePassing0(child, ic);
                }
                if (!a.del.isEmpty()) {
                    for (DelayedAccessor da : a.del) {
                        AbstractDelayerAccessor ada = (AbstractDelayerAccessor) da;
                        Object sidecar = ic.get(this, ada.sidecarType);
                        if (ada instanceof SidecarFieldDelayerAccessor) {
                            SidecarFieldDelayerAccessor sda = (SidecarFieldDelayerAccessor) ada;
                            MethodHandle mh = sda.pra.mh;
                            if (!Modifier.isStatic(sda.pra.field.getModifiers())) {
                                InstantiatedComponentConfiguration icc = ((InstantiatedComponentConfiguration) a);
                                mh = mh.bindTo(icc.instance);
                            }
                            Object ig = sda.pra.afo.invoke(mh);
                            ((BiConsumer) sda.consumer).accept(sidecar, ig);
                        } else {
                            SidecarMethodDelayerAccessor sda = (SidecarMethodDelayerAccessor) ada;
                            MethodHandle mh = sda.pra.mh;
                            if (!Modifier.isStatic(sda.pra.method.getModifiers())) {
                                InstantiatedComponentConfiguration icc = ((InstantiatedComponentConfiguration) a);
                                mh = mh.bindTo(icc.instance);
                            }
                            Object ig = sda.pra.afo.invoke(mh);
                            ((BiConsumer) sda.consumer).accept(sidecar, ig);
                        }
                    }
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
            // This look strange...
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
    public <T extends Extension> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        Extension ce = extensions.get(extensionType);
        if (ce == null) {
            // We do not use computeIfAbsent because extensions might install other extensions.
            // Which would fail with ConcurrentModificationException (see ExtensionDependenciesTest)
            checkConfigurable(); // we can use extensions that have already been installed, but not add new ones
            ce = ExtensionModel.newInstance(this, extensionType);
            extensions.put(extensionType, ce); // make sure we add it here before calling Extension#ExtensionAdded
            AppPackedExtensionSupport.invoke().initializeExtension(ce, this);
        }
        return (T) ce;
    }

    /** {@inheritDoc} */
    @Override
    public WireletList wirelets() {
        return wirelets;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTopContainer() {
        // TODO change when we have hosts.
        return parent == null;
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
// public <T extends Extension> T getExtension(Class<T> extensionType) {
// return (T) extensions.get(extensionType);
// }