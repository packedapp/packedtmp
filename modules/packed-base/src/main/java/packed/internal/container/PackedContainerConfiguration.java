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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.analysis.BundleDescriptor;
import app.packed.analysis.BundleDescriptor.Builder;
import app.packed.base.Contract;
import app.packed.base.Nullable;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.ContainerComposer;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.artifact.BuildOutput;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.component.ComponentModel;
import packed.internal.component.PackedSingletonConfiguration;
import packed.internal.component.PackedStatelessComponentConfiguration;
import packed.internal.config.ConfigSiteUtil;
import packed.internal.hook.applicator.DelayedAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarFieldDelayerAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarMethodDelayerAccessor;
import packed.internal.host.HostConfiguration;
import packed.internal.host.HostConfigurationContext;
import packed.internal.host.PackedHostConfiguration;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;
import packed.internal.inject.util.InjectConfigSiteOperations;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.reflect.OpenClass;
import packed.internal.service.run.DefaultInjector;
import packed.internal.util.UncheckedThrowableFactory;

/** The default implementation of {@link ContainerComposer}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerComposer {

    /** Any extension that is active. */
    @Nullable
    public PackedExtensionContext activeExtension;

    /** Any child containers of this component (lazily initialized), in order of insertion. */
    @Nullable
    ArrayList<PackedContainerConfiguration> containers;

    /** The component that was last installed. */
    @Nullable
    private AbstractComponentConfiguration currentComponent;

    /** All used extensions, in order of registration. */
    private final LinkedHashMap<Class<? extends Extension>, PackedExtensionContext> extensions = new LinkedHashMap<>();

    private HashMap<String, DefaultContainerLayer> layers;

    /** The current component lookup object, updated via {@link #lookup(Lookup)} */
    private ComponentLookup lookup;

    /** A container model object, shared among all container sources of the same type. */
    private final ContainerSourceModel model;

    /** The source of the container configuration. */
    private final Object source;

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
    public PackedContainerConfiguration(BuildOutput output, Object source, Wirelet... wirelets) {
        super(ConfigSiteUtil.captureStackFrame(InjectConfigSiteOperations.INJECTOR_OF), output);
        this.source = requireNonNull(source);
        this.lookup = this.model = ContainerSourceModel.of(source.getClass());
        this.wireletContext = WireletContext.create(this, null, wirelets);
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
    private PackedContainerConfiguration(AbstractComponentConfiguration parent, Bundle bundle, FixedWireletList wirelets) {
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
            for (Object s : e.model().contracts.values()) {
                // TODO need a context
                Contract con;
                if (s instanceof Function) {
                    con = (Contract) ((Function) s).apply(e.extension());
                } else { // BiFcuntoin
                    con = (Contract) ((BiFunction) s).apply(e.extension(), null);
                }
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
            ModuleAccess.container().doConfigure((Bundle) source, this);
        }
        // Initializes the name of the container, and sets the state to State.FINAL
        initializeName(State.FINAL, null);
        super.state.oldState = State.FINAL; // Thing is here, that initialize name returns early if name!=null
    }

    public PackedContainerConfiguration doBuild() {
        configure();
        extensionsContainerConfigured();
        return this;
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

        // TODO we want to cache this at some point....
        for (Class<? extends Extension> c : ExtensionUseModel2.totalOrderOfExtensionReversed(extensions.keySet())) {
            PackedExtensionContext e = extensions.get(c);
            if (e != null) {
                activeExtension = e;
                e.onConfigured();
            }
        }
        activeExtension = null;

        if (children != null) {
            for (AbstractComponentConfiguration acc : children.values()) {
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
        PackedExtensionContext ee = extensions.get(ServiceExtension.class);
        if (ee != null) {
            DefaultInjector di = ModuleAccess.service().toNode(((ServiceExtension) ee.extension())).onInstantiate(ic.wirelets);
            ic.put(this, di);
        }
        super.extensionsPrepareInstantiation(ic);
    }

    /**
     * Used to convert factories to method handle
     * 
     * @param handle
     *            the factory handle
     * @return the method handle
     */
    public MethodHandle fromFactoryHandle(FactoryHandle<?> handle) {
        return lookup.readable(handle).toMethodHandle();
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

    @Override
    public String initializeNameDefaultName() {
        // I think try and move some of this to ComponentNameWirelet
        @Nullable
        Class<?> source = this.sourceType();
        if (Bundle.class.isAssignableFrom(source)) {
            String nnn = source.getSimpleName();
            if (nnn.length() > 6 && nnn.endsWith("Bundle")) {
                nnn = nnn.substring(0, nnn.length() - 6);
            }
            if (nnn.length() > 0) {
                // checkName, if not just App
                // TODO need prefix
                return nnn;
            }
            if (nnn.length() == 0) {
                return "Container";
            }
        }
        // TODO think it should be named Artifact type, for example, app, injector, ...
        return "Unknown";
    }

    /** {@inheritDoc} */
    @Override
    // Flyt til AbstractComponentConfiguration????? Saa det er interfacet der styrer?
    public <T> SingletonConfiguration<T> install(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return install(Factory.find(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        ComponentModel model = lookup.componentModelOf(factory.rawType());
        ConfigSite configSite = captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL);
        PackedSingletonConfiguration<T> conf = new PackedSingletonConfiguration<>(configSite, this, model, (BaseFactory<T>) factory);
        installPrepare(State.INSTALL_INVOKED);
        currentComponent = conf;
        return conf.runHooks(source);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        ComponentModel model = lookup.componentModelOf(instance.getClass());
        ConfigSite configSite = captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL);
        PackedSingletonConfiguration<T> conf = new PackedSingletonConfiguration<>(configSite, this, model, instance);
        installPrepare(State.INSTALL_INVOKED);
        currentComponent = conf;
        return conf.runHooks(source);
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
    public StatelessConfiguration installStateless(Class<?> implementation) {
        requireNonNull(implementation, "implementation is null");
        ComponentModel model = lookup.componentModelOf(implementation);
        ConfigSite configSite = captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL);
        PackedStatelessComponentConfiguration conf = new PackedStatelessComponentConfiguration(configSite, this, model);
        installPrepare(State.INSTALL_INVOKED);
        currentComponent = conf;
        return conf.runHooks(source);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainer instantiate(AbstractComponent parent, PackedArtifactInstantiationContext ic) {
        return new PackedContainer(parent, this, ic);
    }

    public PackedContainer instantiateArtifact(WireletContext wirelets) {
        PackedArtifactInstantiationContext pic = new PackedArtifactInstantiationContext(wirelets);
        extensionsPrepareInstantiation(pic);

        // Will instantiate the whole container hierachy
        PackedContainer pc = new PackedContainer(null, this, pic);
        methodHandlePassing0(pc, pic);
        return pc;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isArtifactRoot() {
        return parent == null || parent instanceof HostConfigurationContext; // TODO change when we have hosts.
    }

    /** {@inheritDoc} */
    @Override
    public void link(Bundle bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        FixedWireletList wl = FixedWireletList.of(wirelets);

        // finalize name of this container
        initializeName(State.LINK_INVOKED, null);
        installPrepare(State.LINK_INVOKED);
        currentComponent = null;// need to clear out current component...

        // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
        // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
        // loop situations, for example, if a bundle recursively links itself which fails by throwing
        // java.lang.StackOverflowError instead of an infinite loop.

        PackedContainerConfiguration dcc = new PackedContainerConfiguration(this, bundle, wl);
        dcc.configure();
        addChild(dcc);

        // We have an extra list for all containers.
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
                                PackedSingletonConfiguration<?> icc = ((PackedSingletonConfiguration<?>) cc);
                                mh = mh.bindTo(icc.instance);
                            }
                            ig = sda.pra.operator.invoke(mh);
                        } else {
                            SidecarMethodDelayerAccessor sda = (SidecarMethodDelayerAccessor) da;
                            MethodHandle mh = sda.pra.mh;
                            if (!Modifier.isStatic(sda.pra.method.getModifiers())) {
                                PackedSingletonConfiguration<?> icc = ((PackedSingletonConfiguration<?>) cc);
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

    /**
     * Creates a new layer.
     * 
     * @param name
     *            the name of layer
     * @param dependencies
     *            dependencies on other layers
     * @return the new layer
     */
    public ContainerLayer newLayer(String name, ContainerLayer... dependencies) {
        HashMap<String, DefaultContainerLayer> l = layers;
        if (l == null) {
            l = layers = new HashMap<>();
        }
        DefaultContainerLayer newLayer = new DefaultContainerLayer(this, name, dependencies);
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
    @Override
    public Class<?> sourceType() {
        return source.getClass();
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
     * @param caller
     *            non-null if it is another extension that is requesting the extension
     * @return the extension's context
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     * @throws InternalExtensionException
     *             if the
     */
    PackedExtensionContext useExtension(Class<? extends Extension> extensionType, @Nullable PackedExtensionContext caller) {
        requireNonNull(extensionType, "extensionType is null");
        PackedExtensionContext pec = extensions.get(extensionType);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (pec == null) {
            // Checks that we are still configurable
            if (caller == null) {
                checkConfigurable();
            } else {
                caller.checkConfigurable();
            }
            initializeName(State.EXTENSION_USED, null); // initializes name of container, if not already set
            extensions.put(extensionType, pec = PackedExtensionContext.of(this, extensionType));
        }
        return pec;
    }

    private HostConfigurationContext addHost() {
        ConfigSite configSite = captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL);
        PackedHostConfiguration conf = new PackedHostConfiguration(configSite, this);
        installPrepare(State.INSTALL_INVOKED);
        currentComponent = conf;
        return conf;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HostConfiguration> T addHost(Class<T> hostType) {
        OpenClass cp = new OpenClass(MethodHandles.lookup(), hostType, true);
        MethodHandle mh = ConstructorFinder.find(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY, HostConfigurationContext.class);
        try {
            return (T) mh.invoke(addHost());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
