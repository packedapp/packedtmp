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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import app.packed.artifact.ArtifactDriver;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Install;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLayer;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionInstantiationContext;
import app.packed.container.extension.ExtensionIntrospectionContext;
import app.packed.contract.Contract;
import app.packed.service.Factory;
import app.packed.util.Nullable;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.container.model.ComponentLookup;
import packed.internal.container.model.ComponentModel;
import packed.internal.container.model.ContainerSourceModel;
import packed.internal.hook.applicator.DelayedAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarFieldDelayerAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarMethodDelayerAccessor;
import packed.internal.module.ModuleAccess;
import packed.internal.service.InjectConfigSiteOperations;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration<Object> implements ContainerConfiguration {

    /** Any child containers of this component (lazily initialized), in order of insertion. */
    @Nullable
    public ArrayList<PackedContainerConfiguration> containers;

    /** All registered extensions, in order of registration. */
    private final LinkedHashMap<Class<? extends Extension>, PackedExtensionContext> extensions = new LinkedHashMap<>();

    private HashMap<String, DefaultLayer> layers;

    /** The current lookup object, updated via {@link #lookup(Lookup)} */
    public ComponentLookup lookup; // Should be more private

    /** A container model object, shared among all container sources of the same type. */
    private final ContainerSourceModel model;

    /** The source of the container configuration. */
    public final ContainerSource source;

    /** Any wirelets that was given by the user when creating this configuration. */
    final WireletList wirelets;

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
        this.lookup = this.model = ContainerSourceModel.of(source.getClass());
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
        this.lookup = this.model = ContainerSourceModel.of(bundle.getClass());
        this.wirelets = requireNonNull(wirelets);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void buildDescriptor(BundleDescriptor.Builder builder, boolean isImage) {
        // If we are an image we have already been built.
        if (!isImage) {
            doBuild();
        }

        builder.setBundleDescription(getDescription());
        builder.setName(getName());
        for (PackedExtensionContext e : extensions.values()) {
            BiConsumer<? super Extension, ? super Builder> c = e.model.bundleBuilder;
            if (c != null) {
                c.accept(e.extension(), builder);
            }
            for (BiFunction<?, ? super ExtensionIntrospectionContext, ?> s : e.model.contracts.values()) {
                // TODO need a context

                Contract con = (Contract) ((BiFunction) s).apply(e.extension(), null);
                requireNonNull(con);
                builder.addContract(con);
            }
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
            ModuleAccess.container().doConfigure(bundle, this);
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
        // Extensions are not processed until the whole artifact has been configured.
        // Mainly because some wirelets needs the parent container to be fully configured
        // before they can be used. For example, downstream service wirelets

        installPrepare(State.GET_NAME_INVOKED);
        for (PackedExtensionContext e : extensions.values()) {
            e.onConfigured();
        }

        WireletContext wc = new WireletContext();
        wc.apply(this, wirelets.toArray());

        if (children != null) {
            for (AbstractComponentConfiguration<?> acc : children.values()) {
                if (acc instanceof PackedContainerConfiguration) {
                    ((PackedContainerConfiguration) acc).extensionsContainerConfigured();
                }
            }
        }
    }

    @Override
    void extensionsPrepareInstantiation(PackedArtifactInstantiationContext ic) {
        for (PackedExtensionContext e : extensions.values()) {
            if (e.model.onInstantiation != null) {
                e.model.onInstantiation.accept(e.extension(), new ExtensionInstantiationContext() {

                    @Override
                    public Class<?> artifactType() {
                        return ic.artifactType();
                    }

                    @Override
                    public <T> @Nullable T get(Class<T> type) {
                        return ic.get(PackedContainerConfiguration.this, type);
                    }

                    @Override
                    public void put(Object obj) {
                        ic.put(PackedContainerConfiguration.this, obj);
                    }

                    @Override
                    public <T> T use(Class<T> type) {
                        return ic.use(PackedContainerConfiguration.this, type);
                    }
                });
            }

        }
        super.extensionsPrepareInstantiation(ic);
    }

    @Nullable
    public PackedExtensionContext getExtension(Class<?> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.get(extensionType);
    }

    public <T> ComponentConfiguration<T> install(Factory<T> factory, ConfigSite configSite) {
        ComponentModel model = lookup.componentModelOf(factory.rawType());
        installPrepare(State.INSTALL_INVOKED);
        FactoryComponentConfiguration<T> cc = new FactoryComponentConfiguration<T>(configSite, this, model, factory);
        currentComponent = cc;
        return model.addExtensionsToContainer(this, cc);
    }

    public <T> ComponentConfiguration<T> installInstance(T instance, ConfigSite configSite) {
        ComponentModel model = lookup.componentModelOf(instance.getClass());
        installPrepare(State.INSTALL_INVOKED);
        InstantiatedComponentConfiguration<T> cc = new InstantiatedComponentConfiguration<T>(configSite, this, model, instance);
        currentComponent = cc;
        return model.addExtensionsToContainer(this, cc);
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

    public <T> ComponentConfiguration<T> installStatic(Class<T> implementation, ConfigSite configSite) {
        ComponentModel descriptor = lookup.componentModelOf(implementation);
        installPrepare(State.INSTALL_INVOKED);
        StaticComponentConfiguration<T> cc = new StaticComponentConfiguration<T>(configSite, this, descriptor, implementation);
        currentComponent = cc;
        return descriptor.addExtensionsToContainer(this, cc);
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

        // finalize name of this container
        initializeName(State.LINK_INVOKED, null);
        installPrepare(State.LINK_INVOKED);

        // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
        // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
        // loop situations, for example, if a bundle recursively links itself which fails by throwing
        // java.lang.StackOverflowError instead of an infinite loop.
        PackedContainerConfiguration dcc = new PackedContainerConfiguration(this, bundle, wl);
        dcc.configure();
        addChild(dcc);

        // in addition to addChild, we also keep track of just the containers.
        if (containers == null) {
            containers = new ArrayList<>(5);
        }
        containers.add(dcc);

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
    private void methodHandlePassing0(AbstractComponent ac, PackedArtifactInstantiationContext ic) {
        if (children != null) {
            for (AbstractComponentConfiguration<?> cc : children.values()) {
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
                            ig = sda.pra.operator.apply(mh);
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

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return (T) useExtension(extensionType).extension();
    }

    public PackedExtensionContext useExtension(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        PackedExtensionContext pec = extensions.get(extensionType);

        // We do not use the computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (pec == null) {
            checkConfigurable(); // only allow installing new extensions if configurable
            extensions.put(extensionType, pec = new PackedExtensionContext(this, ExtensionModel.of(extensionType)));
            initializeName(State.EXTENSION_USED, null);
            pec.initialize();
        }
        return pec;
    }
}
