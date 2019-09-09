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
import app.packed.component.ComponentConfiguration;
import app.packed.component.Install;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLayer;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;
import app.packed.inject.Factory;
import app.packed.util.Nullable;
import packed.internal.access.SharedSecrets;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.container.extension.hook.other.DelayedAccessor;
import packed.internal.container.extension.hook.other.DelayedAccessor.SidecarFieldDelayerAccessor;
import packed.internal.container.extension.hook.other.DelayedAccessor.SidecarMethodDelayerAccessor;
import packed.internal.container.model.ComponentLookup;
import packed.internal.container.model.ComponentModel;
import packed.internal.container.model.ContainerModel;
import packed.internal.inject.InjectConfigSiteOperations;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    /** All registered extensions, in order of registration. */
    private final LinkedHashMap<Class<? extends Extension>, PackedExtensionContext> extensions = new LinkedHashMap<>();

    private HashMap<String, DefaultLayer> layers;

    /** The current lookup object, updated via {@link #lookup(Lookup)} */
    public ComponentLookup lookup; // Should be more private

    /** A container model object, shared among all container sources of the same type. */
    private final ContainerModel model;

    /** The source of the container configuration. */
    public final ContainerSource source;

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
     *            any wirelets specified by the user
     */
    public PackedContainerConfiguration(ArtifactDriver<?> artifactDriver, ContainerSource source, Wirelet... wirelets) {
        super(ConfigSite.captureStack(InjectConfigSiteOperations.INJECTOR_OF), artifactDriver);
        this.source = requireNonNull(source);
        this.lookup = this.model = ContainerModel.from(source.getClass());
        this.wirelets = WireletList.of(wirelets);
    }

    /**
     * Creates a new container configuration via {@link #link(Bundle, Wirelet...)}.
     * 
     * @param parent
     *            the parent container configuration
     * @param bundle
     *            the bundle that was linked
     * @param wirelets
     *            any wirelets specified by the user
     */
    private PackedContainerConfiguration(PackedContainerConfiguration parent, Bundle bundle, WireletList wirelets) {
        super(parent.configSite().thenCaptureStackFrame(InjectConfigSiteOperations.INJECTOR_OF), parent);
        this.source = requireNonNull(bundle);
        this.lookup = this.model = ContainerModel.from(bundle.getClass());
        this.wirelets = requireNonNull(wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactBuildContext buildContext() {
        return buildContext;
    }

    public void buildDescriptor(BundleDescriptor.Builder builder, boolean isImage) {
        if (!isImage) {
            doBuild();
        }
        builder.setBundleDescription(getDescription());
        builder.setName(getName());
        for (PackedExtensionContext e : extensions.values()) {
            SharedSecrets.extension().buildBundle(e.extension(), builder);
        }
        builder.extensions.addAll(extensions.keySet());
    }

    /**
     * Configures the configuration.
     */
    private void configure() {
        if (source instanceof Bundle) {
            Bundle bundle = (Bundle) source;
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                // Hmm don't know about that config site
                installInstance(bundle, configSite());
            }
            SharedSecrets.container().doConfigure(bundle, this);
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
        return Collections.unmodifiableSet(extensions.keySet());
    }

    void extensionsContainerConfigured() {
        installPrepare(State.GET_NAME_INVOKED);
        for (PackedExtensionContext e : extensions.values()) {
            e.onConfigured();
        }
        if (children != null) {
            for (AbstractComponentConfiguration acc : children.values()) {
                if (acc instanceof PackedContainerConfiguration) {
                    ((PackedContainerConfiguration) acc).extensionsContainerConfigured();
                }
            }
        }
    }

    @Override
    void extensionsPrepareInstantiation(PackedArtifactInstantiationContext ic) {
        for (PackedExtensionContext e : extensions.values()) {
            SharedSecrets.extension().onPrepareContainerInstantiation(e.extension(), ic);
        }
        super.extensionsPrepareInstantiation(ic);
    }

    public ComponentConfiguration install(Factory<?> factory, ConfigSite configSite) {
        ComponentModel model = lookup.componentModelOf(factory.rawType());
        installPrepare(State.INSTALL_INVOKED);
        return model.addExtensions(this, currentComponent = new FactoryComponentConfiguration(configSite, this, model, factory));
    }

    public ComponentConfiguration installStatic(Class<?> implementation, ConfigSite configSite) {
        ComponentModel descriptor = lookup.componentModelOf(implementation);
        installPrepare(State.INSTALL_INVOKED);
        return descriptor.addExtensions(this, currentComponent = new StaticComponentConfiguration(configSite, this, descriptor, implementation));
    }

    public ComponentConfiguration installInstance(Object instance, ConfigSite configSite) {
        ComponentModel model = lookup.componentModelOf(instance.getClass());
        installPrepare(State.INSTALL_INVOKED);
        return model.addExtensions(this, currentComponent = new InstantiatedComponentConfiguration(configSite, this, model, instance));
    }

    private void installPrepare(State state) {
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
    PackedArtifactContext instantiate(AbstractComponent parent, PackedArtifactInstantiationContext ic) {
        return new PackedArtifactContext(parent, this, ic);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTopContainer() {
        return parent == null; // TODO change when we have hosts.
    }

    /** {@inheritDoc} */
    @Override
    public void link(Bundle bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        WireletList wl = WireletList.of(wirelets);

        initializeName(State.LINK_INVOKED, null);
        installPrepare(State.LINK_INVOKED);

        // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
        // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
        // loop situations, for example, if a bundle recursively links itself which fails by throwing
        // java.lang.StackOverflowError instead of an infinite loop.
        PackedContainerConfiguration dcc = new PackedContainerConfiguration(this, bundle, wl);
        dcc.configure();
        addChild(dcc);

        // Previously this method returned the specified bundle. However, to encourage people to configure the bundle before
        // calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have void return
        // type.
        // Maybe in the future LinkedBundle<- (LinkableContainerSource)
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        // If user specifies null, we use whatever
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? model : model.withLookup(lookup);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void methodHandlePassing0(AbstractComponent ac, ArtifactInstantiationContext ic) {
        if (children != null) {
            for (AbstractComponentConfiguration cc : children.values()) {
                AbstractComponent child = ac.children.get(cc.name);
                if (cc instanceof PackedContainerConfiguration) {
                    ((PackedContainerConfiguration) cc).methodHandlePassing0(child, ic);
                }
                if (!cc.del.isEmpty()) {
                    for (DelayedAccessor da : cc.del) {
                        Object sidecar = ic.get(this, da.sidecarType);
                        Object ig;
                        if (da instanceof SidecarFieldDelayerAccessor) {
                            SidecarFieldDelayerAccessor sda = (SidecarFieldDelayerAccessor) da;
                            MethodHandle mh = sda.pra.mh;
                            if (!Modifier.isStatic(sda.pra.field.getModifiers())) {
                                InstantiatedComponentConfiguration icc = ((InstantiatedComponentConfiguration) cc);
                                mh = mh.bindTo(icc.instance);
                            }
                            ig = sda.pra.operator.invoke(mh);
                        } else {
                            SidecarMethodDelayerAccessor sda = (SidecarMethodDelayerAccessor) da;
                            MethodHandle mh = sda.pra.mh;
                            if (!Modifier.isStatic(sda.pra.method.getModifiers())) {
                                InstantiatedComponentConfiguration icc = ((InstantiatedComponentConfiguration) cc);
                                mh = mh.bindTo(icc.instance);
                            }
                            ig = sda.pra.operator.invoke(mh);
                        }
                        ((BiConsumer) da.consumer).accept(sidecar, ig);
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

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Extension> T getExtension(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        PackedExtensionContext pec = extensions.get(extensionType);
        return pec == null ? null : (T) pec.extension();
    }

    public PackedExtensionContext useContext(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        PackedExtensionContext pec = extensions.get(extensionType);

        // We do not use the computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (pec == null) {
            checkConfigurable(); // only allow installing new extensions if configurable
            extensions.put(extensionType, pec = PackedExtensionContext.create(this, extensionType));
            SharedSecrets.extension().initializeExtension(pec);
        }
        return pec;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return (T) useContext(extensionType).extension();
    }

    /** {@inheritDoc} */
    @Override
    public WireletList wirelets() {
        return wirelets;
    }
}
