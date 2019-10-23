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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import app.packed.api.Contract;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLayer;
import app.packed.container.ContainerSource;
import app.packed.container.Extension;
import app.packed.container.ExtensionDescriptorContext;
import app.packed.container.ExtensionInstantiationContext;
import app.packed.container.ExtensionWirelet.Pipeline;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.lang.Nullable;
import app.packed.service.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.artifact.BuildOutput;
import packed.internal.artifact.PackedArtifactContext;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.component.ComponentModel;
import packed.internal.component.CoreComponentConfiguration;
import packed.internal.component.FactoryComponentConfiguration;
import packed.internal.component.InstantiatedComponentConfiguration;
import packed.internal.component.StaticComponentConfiguration;
import packed.internal.config.ConfigSiteUtil;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.hook.applicator.DelayedAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarFieldDelayerAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarMethodDelayerAccessor;
import packed.internal.inject.util.InjectConfigSiteOperations;
import packed.internal.moduleaccess.ModuleAccess;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration<Object> implements ContainerConfiguration {

    @Nullable
    public PackedExtensionContext activeExtension;

    /** Any child containers of this component (lazily initialized), in order of insertion. */
    @Nullable
    public ArrayList<PackedContainerConfiguration> containers;

    /** The component that was last installed. */
    @Nullable
    protected CoreComponentConfiguration<?> currentComponent;

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
    public final WireletContext wireletContext;

    /**
     * Creates a new container configuration.
     * 
     * @param output
     *            the build output
     * @param source
     *            the source of the container
     * @param wirelets
     *            any wirelets specified by the user
     */
    public PackedContainerConfiguration(BuildOutput output, ContainerSource source, Wirelet... wirelets) {
        super(ConfigSiteUtil.captureStackFrame(InjectConfigSiteOperations.INJECTOR_OF), output);
        this.source = requireNonNull(source);
        this.lookup = this.model = ContainerSourceModel.of(source.getClass());
        this.wireletContext = WireletContext.create(this, null, wirelets);
    }

    /**
     * Creates a new container configuration when {@link #link(Bundle, Wirelet...) linking a bundle}.
     * 
     * @param parent
     *            the parent container configuration
     * @param bundle
     *            the bundle that was linked
     * @param wirelets
     *            any wirelets specified by the user
     */
    private PackedContainerConfiguration(PackedContainerConfiguration parent, Bundle bundle, FixedWireletList wirelets) {
        super(ConfigSiteUtil.captureStackFrame(parent.configSite(), InjectConfigSiteOperations.INJECTOR_OF), parent);
        this.source = requireNonNull(bundle);
        this.lookup = this.model = ContainerSourceModel.of(bundle.getClass());
        this.wireletContext = WireletContext.create(this, null, wirelets);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void buildDescriptor(BundleDescriptor.Builder builder) {
        builder.setBundleDescription(getDescription());
        builder.setName(getName());
        for (PackedExtensionContext e : extensions.values()) {
            BiConsumer<? super Extension, ? super Builder> c = e.model().bundleBuilder;
            if (c != null) {
                c.accept(e.extension(), builder);
            }
            for (BiFunction<?, ? super ExtensionDescriptorContext, ?> s : e.model().contracts.values()) {
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
            // if (bundle.getClass().isAnnotationPresent(Install.class)) {
            // // Hmm don't know about that config site
            // // installInstance(bundle, configSite());
            // }
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

    public PackedArtifactContext doInstantiate(WireletContext wirelets) {
        PackedArtifactInstantiationContext pic = new PackedArtifactInstantiationContext(wirelets);
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
        // We could actually end of installing new extensions...

        // Here we need sorting...
        LinkedHashSet<Class<? extends Extension>> s = new LinkedHashSet<>();
        if (extensions.containsKey(ComponentExtension.class)) {
            s.add(ComponentExtension.class);
        }
        if (extensions.containsKey(ServiceExtension.class)) {
            s.add(ServiceExtension.class);
        }
        s.addAll(extensions.keySet());
        ArrayList<Class<? extends Extension>> l = new ArrayList<>(s);
        Collections.reverse(l);

        for (Class<? extends Extension> c : l) {
            PackedExtensionContext e = extensions.get(c);
            // System.out.println("On Configured " + e.model.extensionType);
            activeExtension = e;
            e.onConfigured();
        }
        activeExtension = null;

        if (children != null) {
            for (AbstractComponentConfiguration<?> acc : children.values()) {
                if (acc instanceof PackedContainerConfiguration) {
                    ((PackedContainerConfiguration) acc).extensionsContainerConfigured();
                }
            }
        }

        // TODO, fix for unused wirelet, it was removed to fix other stuff..
        // if (wireletContext != null) {
        // for (Class<? extends Pipeline<?, ?, ?>> cc : wireletContext.pipelines.keySet()) {
        // // List<ExtensionWirelet<?>> ll = wireletContext.pipelines.get(cc);
        // // throw new IllegalArgumentException("The wirelets " + ll + " requires the extension " + cc.getSimpleName() + " to
        // be
        // // installed.");
        //
        // }
        // }
    }

    @Override
    protected void extensionsPrepareInstantiation(PackedArtifactInstantiationContext ic) {
        for (PackedExtensionContext e : extensions.values()) {
            if (e.model().onInstantiation != null) {
                e.model().onInstantiation.accept(e.extension(), new ExtensionInstantiationContext() {
                    @Override
                    public Class<?> artifactType() {
                        return ic.artifactType();
                    }

                    @Nullable
                    @Override
                    public <T> T get(Class<T> type) {
                        return ic.get(PackedContainerConfiguration.this, type);
                    }

                    @Override
                    public <T extends Pipeline<?, ?, ?>> T getPipeline(Class<T> pipelineType) {

                        // uncommented temporary to get WTest to run.

                        // We need to check that someone does not request another extensions pipeline type.
                        // if (!e.model.pipelines.containsKey(pipelineType)) {
                        // throw new ExtensionDeclarationException("The specified pipeline type is not amongst " + e.type().getSimpleName()
                        // + " pipeline types, pipelineType = " + pipelineType);
                        // }
                        return ic.wirelets.getPipelin(pipelineType);
                    }

                    @Override
                    public boolean isFromImage() {
                        return false;
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

    /**
     * Returns the context for the specified extension type. Or null if no extension of the specified type is installed.
     * 
     * @param extensionType
     *            the type of extension to return a context for
     * @return an extension's context, iff on of the specified type is already installed
     * @see #use(Class)
     * @see #useExtension(Class, PackedExtensionContext)
     */
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
    protected PackedArtifactContext instantiate(AbstractComponent parent, PackedArtifactInstantiationContext ic) {
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
        FixedWireletList wl = FixedWireletList.of(wirelets);

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

    // /** {@inheritDoc} */
    // @Override
    // public void link(Class<? extends Bundle> bundle, Wirelet... wirelets) {
    // requireNonNull(bundle, "bundle is null");
    // ContainerSourceModel csm = ContainerSourceModel.of(bundle);
    // Bundle b;
    // try {
    // b = (Bundle) csm.emptyConstructor().invoke();
    // } catch (Throwable e) {
    // ThrowableUtil.rethrowErrorOrRuntimeException(e);
    // throw new UndeclaredThrowableException(e);
    // }
    // link(b, wirelets);
    // }

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
        return (T) useExtension(extensionType, null).extension();
    }

    /**
     * If an extension of the specified type has not already been installed, installs it. Returns the extension's context.
     * 
     * @param extensionType
     *            the type of extension
     * @param callingExtension
     *            non-null if it is another extension that is requesting the extension
     * @return the extension's context
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     * @throws InternalExtensionException
     *             if the
     */
    public PackedExtensionContext useExtension(Class<? extends Extension> extensionType, @Nullable PackedExtensionContext callingExtension) {
        requireNonNull(extensionType, "extensionType is null");
        PackedExtensionContext pec = extensions.get(extensionType);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (pec == null) {
            // Checks that we are still configurable
            if (callingExtension == null) {
                checkConfigurable();
            } else {
                callingExtension.checkConfigurable();
            }
            initializeName(State.EXTENSION_USED, null); // initializes name of container, if not already set

            // Retrieves the extension model for the specified extension. This will throw ExtensionDeclarationException
            // If the extension is not correctly implemented.
            ExtensionModel<? extends Extension> model = ExtensionModel.of(extensionType);

            extensions.put(extensionType, pec = new PackedExtensionContext(this, model));
            pec.initialize(this); // initializes the extension, might use additional new extensions
        }
        return pec;
    }
}
