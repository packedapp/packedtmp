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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.api.Contract;
import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLayer;
import app.packed.container.ContainerSource;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.lang.Nullable;
import app.packed.service.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.artifact.BuildOutput;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.component.ComponentModel;
import packed.internal.component.PackedSingletonConfiguration;
import packed.internal.component.PackedStatelessComponentConfiguration;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.config.ConfigSiteUtil;
import packed.internal.hook.applicator.DelayedAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarFieldDelayerAccessor;
import packed.internal.hook.applicator.DelayedAccessor.SidecarMethodDelayerAccessor;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.util.InjectConfigSiteOperations;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.service.run.DefaultInjector;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration<Void> implements ContainerConfiguration {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** Any extension that is active. */
    @Nullable
    public PackedExtensionContext activeExtension;

    /** Any child containers of this component (lazily initialized), in order of insertion. */
    @Nullable
    ArrayList<PackedContainerConfiguration> containers;

    /** The component that was last installed. */
    @Nullable
    private AbstractComponentConfiguration<?> currentComponent;

    /** All registered extensions, in order of registration. */
    private final LinkedHashMap<Class<? extends Extension>, PackedExtensionContext> extensions = new LinkedHashMap<>();

    private HashMap<String, DefaultContainerLayer> layers;

    /** The current component lookup object, updated via {@link #lookup(Lookup)} */
    private ComponentLookup lookup;

    /** A container model object, shared among all container sources of the same type. */
    private final ContainerSourceModel model;

    /** The source of the container configuration. */
    private final ContainerSource source;

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

    @Override
    protected String initializeNameDefaultName() {
        // I think try and move some of this to ComponentNameWirelet
        @Nullable
        Class<? extends ContainerSource> source = this.sourceType();
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
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements {@link ContainerSource}.
     * <p>
     * Invoking this method typically takes in the order of 1-2 microseconds.
     * <p>
     * If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
     * {@link ConfigSite#UNKNOWN}.
     * 
     * @param operation
     *            the operation
     * @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
     * @see StackWalker
     */
    // TODO add stuff about we also ignore non-concrete container sources...
    protected final ConfigSite captureStackFrame(String operation) {
        // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
        // to the extension class in order to simplify the filtering mechanism.

        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    /**
     * @param frame
     *            the frame to filter
     * @return whether or not to filter the frame
     */
    private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {
        Class<?> c = frame.getDeclaringClass();
        // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
        // Syntes bare vi filtrer app.packed.base modulet fra...
        // Kan vi ikke checke om imod vores container source.

        // ((PackedExtensionContext) context()).container().source
        // Nah hvis man koere fra config er det jo fint....
        // Fra config() paa en bundle er det fint...
        // Fra alt andet ikke...

        // Dvs ourContainerSource
        return Extension.class.isAssignableFrom(c)
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && ContainerSource.class.isAssignableFrom(c));
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
        super.state = State.FINAL; // Thing is here, that initialize name returns early if name!=null
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

    /** {@inheritDoc} */
    @Override
    public <T> ComponentConfiguration<T> install(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return install(Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public <T> ComponentConfiguration<T> install(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        ConfigSite configSite = captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL);
        ComponentModel model = lookup.componentModelOf(factory.rawType());
        PackedSingletonConfiguration<T> cc = new PackedSingletonConfiguration<>(configSite, this, model, factory);
        installPrepare(State.INSTALL_INVOKED);
        currentComponent = cc;
        return cc.runHooks(source);
    }

    /** {@inheritDoc} */
    @Override
    public <T> ComponentConfiguration<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        ConfigSite configSite = captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL);
        ComponentModel model = lookup.componentModelOf(instance.getClass());
        PackedSingletonConfiguration<T> cc = new PackedSingletonConfiguration<>(configSite, this, model, instance);
        installPrepare(State.INSTALL_INVOKED);
        currentComponent = cc;
        return cc.runHooks(source);
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

    /**
     * Installs a stateless component.
     * <p>
     * This method uses the {@link ServiceExtension}.
     * 
     * @param <T>
     *            the type of the component
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    @Override
    public <T> ComponentConfiguration<T> installStateless(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        ConfigSite configSite = captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL);
        ComponentModel descriptor = lookup.componentModelOf(implementation);
        PackedStatelessComponentConfiguration<T> cc = new PackedStatelessComponentConfiguration<T>(configSite, this, descriptor, implementation);
        installPrepare(State.INSTALL_INVOKED);
        currentComponent = cc;
        return cc.runHooks(source);
    }

    /** {@inheritDoc} */
    @Override
    protected PackedContainer instantiate(AbstractComponent parent, PackedArtifactInstantiationContext ic) {
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

    /** {@inheritDoc} */
    @Override
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
    public Class<? extends ContainerSource> sourceType() {
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
    public PackedExtensionContext useExtension(Class<? extends Extension> extensionType, @Nullable PackedExtensionContext caller) {
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
}
