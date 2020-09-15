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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.base.AttributeMap;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.TreePath;
import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentAttributes;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.service.ExportedServiceConfiguration;
import packed.internal.base.attribute.DefaultAttributeMap;
import packed.internal.base.attribute.PackedAttribute;
import packed.internal.base.attribute.ProvidableAttributeModel;
import packed.internal.base.attribute.ProvidableAttributeModel.Attt;
import packed.internal.component.wirelet.InternalWirelet.ComponentNameWirelet;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.ContainerAssembly;
import packed.internal.container.ExtensionAssembly;
import packed.internal.container.ExtensionModel;
import packed.internal.inject.various.ConfigSiteInjectOperations;
import packed.internal.service.InjectionManager;
import packed.internal.util.ThrowableUtil;

/** The build time representation of a component. */
public final class ComponentNodeConfiguration extends OpenTreeNode<ComponentNodeConfiguration> implements ComponentConfigurationContext {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The assembly this configuration is a part of. */
    private final PackedAssemblyContext assembly;

    /** The driver used to create this configuration. */
    private final PackedComponentDriver<?> driver;

    /** The realm the component belongs to. */
    private final PackedRealm realm;

    /** Any wirelets that was specified by the user when creating this configuration. */
    @Nullable
    final WireletPack wirelets;

    /** The modifiers of this configuration. */
    final int modifiers;

    /**************** ASSEMBLIES *****************/

    /** The region this component is a part of. */
    public final RegionAssembly region;

    /** Any container this component is part of. A container is part of it self */
    @Nullable
    public final ContainerAssembly memberOfContainer;

    /** Any container this component is part of. A container is part of it self */
    @Nullable
    public final ContainerAssembly container;

    // public final ContainerAssembly memberOfContainer-> a container uses itself

    // public final RealmAssemly realmUser -> a realm component uses itself

    /** Any extension that is attached to this component. */
    @Nullable
    public final ExtensionAssembly extension;

    /** Any source that is attached to this component. */
    @Nullable
    public final SourceAssembly source;

    /**************** See how much of this we can get rid of. *****************/

    /** The configuration site of this component. */
    private final ConfigSite configSite;

    private boolean finalState = false;

    int nameState;

    private static final int NAME_INITIALIZED_WITH_WIRELET = 1 << 18; // set atomically with DONE
    private static final int NAME_SET = 1 << 17; // set atomically with ABNORMAL
    private static final int NAME_GET = 1 << 16; // true if joiner waiting
    private static final int NAME_GET_PATH = 1 << 15; // true if joiner waiting
    private static final int NAME_CHILD_GOT_PATH = 1 << 14; // true if joiner waiting

    private static final int NAME_GETSET_MASK = NAME_SET + NAME_GET + NAME_GET_PATH + NAME_CHILD_GOT_PATH;

    /**
     * Creates a new instance of this class
     * 
     * @param configSite
     *            the configuration site of the component
     * @param parent
     *            the parent of the component
     */
    private ComponentNodeConfiguration(PackedAssemblyContext assembly, PackedRealm realm, PackedComponentDriver<?> driver, ConfigSite configSite,
            @Nullable ComponentNodeConfiguration parent, @Nullable WireletPack wirelets) {
        super(parent);
        this.assembly = requireNonNull(assembly);
        this.driver = requireNonNull(driver);
        this.configSite = requireNonNull(configSite);
        this.wirelets = wirelets;

        int mod = driver.modifiers;
        if (parent == null) {
            this.region = new RegionAssembly(this); // Root always needs a nodestore

            mod = mod | assembly.modifiers;
            mod = PackedComponentModifierSet.add(mod, ComponentModifier.SYSTEM);
            if (assembly.modifiers().isGuest()) {
                // Is it a guest if we are analyzing??? Well we want the information...
                mod = PackedComponentModifierSet.add(mod, ComponentModifier.GUEST);
            }
        } else {
            this.region = driver.modifiers().isGuest() ? new RegionAssembly(this) : parent.region;
        }
        this.modifiers = mod;

        // Setup Realm
        this.realm = requireNonNull(realm);
        if (realm.compConf == null) {
            realm.compConf = this;
        }

        // Setup Container
        if (modifiers().isContainer()) {
            region.reserve();
            this.memberOfContainer = this.container = new ContainerAssembly(this);
        } else {
            this.container = null;
            this.memberOfContainer = parent == null ? null : parent.memberOfContainer;
        }

        // Setup Guest
        if (modifiers().isGuest()) {
            region.reserve(); // reserve a slot to an instance of PackedGuest
        }

        // Setup Source
        if (modifiers().isSource()) {
            this.source = new SourceAssembly(this, region, realm, driver.data);
            this.source.model.invokeOnHookOnInstall(this);
        } else {
            this.source = null;
        }

        // Setup Extension
        this.extension = null; // Extensions uses another constructor

        // Setup default name
        setName0(null);
    }

    public ComponentNodeConfiguration(ComponentNodeConfiguration parent, ExtensionModel model) {
        super(parent);
        this.assembly = requireNonNull(parent.assembly);
        this.driver = requireNonNull(model.driver);
        this.configSite = parent.configSite();
        this.wirelets = null;
        this.modifiers = PackedComponentModifierSet.add(0, ComponentModifier.EXTENSION);
        this.region = parent.region;
        this.realm = PackedRealm.fromExtension(model.type());
        this.container = null;
        this.memberOfContainer = parent.container;
        this.source = null;
        this.extension = new ExtensionAssembly(this, (ExtensionModel) driver.data);
        setName0(null /* model.nameComponent */); // setName0(String) does not work currently
    }

    /**
     * Returns a {@link Component} adaptor of this node.
     * 
     * @return a component adaptor
     */
    public Component adaptToComponent() {
        return new ComponentAdaptor(this);
    }

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements
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
    ConfigSite captureStackFrame(String operation) {
        // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
        // to the extension class in order to simplify the filtering mechanism.

        // Vi kan spoerge "if context.captureStackFrame() ...."

        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    AttributeMap attributes() {
        // Det er ikke super vigtigt at den her er hurtig paa configurations tidspunktet...

        // Maaske er det simpelthen et view...
        // Hvor vi lazily fx calculere EntrySet (og gemmer i et felt)

        DefaultAttributeMap dam = new DefaultAttributeMap();

        if (source != null) {
            dam.addValue(ComponentAttributes.SOURCE_TYPE, source.model.modelType());
        }

        if (extension != null) {
            dam.addValue(ComponentAttributes.EXTENSION_MEMBER, extension.extensionType());
            ProvidableAttributeModel pam = extension.model().pam();
            if (pam != null) {
                for (Entry<PackedAttribute<?>, Attt> e : pam.attributeTypes.entrySet()) {
                    Extension ex = extension.instance();
                    Object val;
                    MethodHandle mh = e.getValue().mh;
                    try {
                        val = mh.invoke(ex);
                    } catch (Throwable e1) {
                        throw ThrowableUtil.orUndeclared(e1);
                    }

                    if (val == null) {
                        if (!e.getValue().isNullable) {
                            throw new IllegalStateException("CANNOT ADD NULL " + e.getKey());
                        }
                    } else {
                        dam.addValue((PackedAttribute) e.getKey(), val);
                    }
                }
            }
        }

        if (PackedComponentModifierSet.isSet(modifiers, ComponentModifier.SHELL)) {
            dam.addValue(ComponentAttributes.SHELL_TYPE, assembly.shellDriver().rawType());
        }
        return dam;
    }

    /**
     * @param frame
     *            the frame to filter
     * @return whether or not to filter the frame
     */
    private boolean captureStackFrameIgnoreFilter(StackFrame frame) {
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
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && Bundle.class.isAssignableFrom(c));
    }

    /** {@inheritDoc} */
    @Override
    public PackedAssemblyContext assembly() {
        return assembly;
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (finalState) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    public ComponentNodeConfiguration assembledSuccesfully() {
        finalState = true;
        if (container != null) {
            container.advanceTo(ContainerAssembly.LS_3_FINISHED);
        }
        region.assemblyClosed();
        return this;
    }

    public void bundleDone() {
        finalState = true;
        for (ComponentNodeConfiguration compConf = treeFirstChild; compConf != null; compConf = compConf.treeNextSibling) {
            compConf.bundleDone();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return configSite;
    }

    /**
     * Returns the container this component is a part of. Or null if this component is the top level container.
     * 
     * @return the container this component is a part of
     */
    @Nullable
    public ContainerAssembly container() {
        return memberOfContainer;
    }

    /**
     * Returns the driver of this component.
     * 
     * @return the driver of this component
     */
    public PackedComponentDriver<?> driver() {
        return driver;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        // Only update with NAME_GET if no prev set/get op
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET;
        return name;
    }

    public InjectionManager injectionManager() {
        return memberOfContainer.im;
    }

    // Previously this method returned the specified bundle. However, to encourage people to configure the bundle before
    // calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have void return
    // type. Maybe in the future LinkedBundle<- (LinkableContainerSource)
    // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
    // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
    // loop situations, for example, if a bundle recursively links itself which fails by throwing
    // java.lang.StackOverflowError instead of an infinite loop.
    @Override
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        // Get the driver from the bundle
        PackedComponentDriver<?> driver = BundleHelper.getDriver(bundle);

        if (container != null) {
            // IDK do we want to progress to next stage just in case...
            if (container.containerState == ContainerAssembly.LS_0_MAINL) {
                container.advanceTo(ContainerAssembly.LS_1_LINKING);
            } else if (container.containerState == ContainerAssembly.LS_2_HOSTING) {
                throw new IllegalStateException("Was hosting");
            } else if (container.containerState == ContainerAssembly.LS_3_FINISHED) {
                throw new IllegalStateException("Was Assembled");
            }
        }
        // Create the child node
        // ConfigSite cs = ConfigSiteSupport.captureStackFrame(configSite(), ConfigSiteInjectOperations.INJECTOR_OF);
        WireletPack wp = WireletPack.from(driver, wirelets);
        ConfigSite cs = ConfigSite.UNKNOWN;
        ComponentNodeConfiguration p = driver().modifiers().isExtension() ? treeParent : this;
        ComponentNodeConfiguration newNode = p.newChild(driver, cs, PackedRealm.fromBundle(bundle), wp);

        // Invoke Bundle::configure
        BundleHelper.configure(bundle, driver.toConfiguration(newNode));

        newNode.finalState = true;
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    public ComponentNodeConfiguration newChild(PackedComponentDriver<?> driver, ConfigSite configSite, PackedRealm realm, @Nullable WireletPack wp) {
        return new ComponentNodeConfiguration(assembly, realm, driver, configSite, this, wp);
    }

    @Nullable
    public ComponentNodeConfiguration getParent() {
        return treeParent;
    }

    /** {@inheritDoc} */
    @Override
    public TreePath path() {
        int anyPathMask = NAME_GET_PATH + NAME_CHILD_GOT_PATH;
        if ((nameState & anyPathMask) != 0) {
            ComponentNodeConfiguration p = treeParent;
            while (p != null && ((p.nameState & anyPathMask) == 0)) {
                p.nameState = (p.nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
            }
        }
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
        return PackedTreePath.of(this); // show we weak intern them????
    }

    public PackedRealm realm() {
        return realm;
    }

    public <W extends Wirelet> Optional<W> receiveWirelet(Class<W> type) {
        if (wirelets == null) {
            return Optional.empty();
        }
        W w = wirelets.receiveLast(type);
        return Optional.ofNullable(w);
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        // First lets check the name is valid
        ComponentNameWirelet.checkName(name);
        int s = nameState;

        checkConfigurable();

        // maybe assume s==0

        if ((s & NAME_SET) != 0) {
            throw new IllegalStateException("#setName(String) can only be called once");
        }

        if ((s & NAME_GET) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #getName() has been invoked");
        }

        if ((s & NAME_GET_PATH) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #path() has been invoked");
        }

        if ((s & NAME_CHILD_GOT_PATH) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #path() has been invoked on a child component");
        }

        // Maaske kan vi godt saette to gange...
        nameState |= NAME_SET;

        if ((s & NAME_INITIALIZED_WITH_WIRELET) != 0) {
            return;// We never set override a name set by a wirelet
        }

        setName0(name);
    }

    private void setName0(String newName) {
        String n = newName;
        if (newName == null) {
            if (wirelets != null) {
                String nameName = wirelets.nameWirelet();
                if (nameName != null) {
                    nameState = NAME_INITIALIZED_WITH_WIRELET;
                    n = nameName;
                }
            }
        }

        boolean isFree = false;

        if (n == null) {
            if (source != null) {
                n = source.model.defaultPrefix();
            } else {
                n = driver.defaultName(realm);
            }
            isFree = true;
        } else if (n.endsWith("?")) {
            n = n.substring(0, n.length() - 1);
            isFree = true;
        }

        // maybe just putIfAbsent, under the assumption that we will rarely need to override.
        if (treeParent != null) {
            if (treeParent.treeChildren != null && treeParent.treeChildren.containsKey(n)) {
                // If name exists. Lets keep a counter (maybe if bigger than 5). For people trying to
                // insert a given component 1 million times...
                if (!isFree) {
                    throw new RuntimeException("Name already exist " + n);
                }
                int counter = 1;
                String prefix = n;
                do {
                    n = prefix + counter++;
                } while (treeParent.treeChildren.containsKey(n));
            }

            if (newName != null) {
                // TODO check if changed name...
                treeParent.treeChildren.remove(name);
                treeParent.treeChildren.put(n, this);
            } else {
                name = n;
                if (treeParent.treeChildren == null) {
                    treeParent.treeChildren = new HashMap<>();
                    treeParent.treeFirstChild = treeParent.treeLastChild = this;
                } else {
                    treeParent.treeLastChild.treeNextSibling = this;
                    treeParent.treeLastChild = this;
                }
                treeParent.treeChildren.put(n, this);
            }
        }
        name = n;
    }

    /** {@inheritDoc} */
    @Override
    public <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        PackedComponentDriver<C> d = (PackedComponentDriver<C>) requireNonNull(driver, "driver is null");
        WireletPack wp = WireletPack.from(d, wirelets);
        ConfigSite configSite = captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);

        ComponentNodeConfiguration conf;
        if (extension == null) {
            conf = newChild(d, configSite, realm, wp);
        } else {
            // When an extension adds new components they are added to the container (the extension's parent)
            // Instead of the extension, because the extension itself is removed at runtime.
            conf = treeParent.newChild(d, configSite, /* The extension's realm */ realm, wp);
        }
        return d.toConfiguration(conf);
    }

    public static ComponentNodeConfiguration newAssembly(PackedAssemblyContext assembly, PackedComponentDriver<?> driver, ConfigSite configSite,
            PackedRealm realm, WireletPack wirelets) {
        return new ComponentNodeConfiguration(assembly, realm, driver, configSite, null, wirelets);
    }

    // This should only be called by special methods
    // We just take the lookup to make sure caller think twice before calling this method.
    public static ComponentNodeConfiguration unadapt(Lookup caller, Component component) {
        if (!(component instanceof ComponentAdaptor)) {
            throw new IllegalStateException("This method must be called before a component is instantiated");
        }
        ComponentAdaptor cc = (ComponentAdaptor) component;
        return cc.compConf;
    }

    @Override
    public void sourceProvide() {
        checkConfigurable();
        checkHasSource();
        source.provide();
    }

    @Override
    public Optional<Key<?>> sourceProvideAsKey() {
        checkHasSource();
        return source.service == null ? Optional.empty() : Optional.of(source.service.key());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) injectionManager().exports().export(source.service,
                captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        if (source == null) {
            throw new UnsupportedOperationException();
        }
        source.provide().as((Key) key);
    }

    /* public methods */

    /** Checks that this component has a source. */
    private void checkHasSource() {
        if (source == null) {
            throw new UnsupportedOperationException(
                    "This method can only be called component that has the " + ComponentModifier.class.getSimpleName() + ".SOURCE modifier set");
        }
    }

    /** Checks that this component has a source. */
    private void checkHasContainer() {
        if (container == null) {
            throw new UnsupportedOperationException(
                    "This method can only be called component that has the " + ComponentModifier.class.getSimpleName() + ".CONTAINER modifier set");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> containerExtensions() {
        checkHasContainer();
        return container.extensionView();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T containerUse(Class<T> extensionType) {
        checkHasContainer();
        return container.useExtension(extensionType);
    }

    /** An adaptor of the {@link Component} interface from a {@link ComponentNodeConfiguration}. */
    private static final class ComponentAdaptor implements Component {

        /** The component configuration to wrap. */
        private final ComponentNodeConfiguration compConf;

        private ComponentAdaptor(ComponentNodeConfiguration compConf) {
            this.compConf = requireNonNull(compConf);
        }

        /** {@inheritDoc} */
        @Override
        public AttributeMap attributes() {
            return compConf.attributes();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            return compConf.toList(ComponentNodeConfiguration::adaptToComponent);
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configSite() {
            return compConf.configSite(); // We might need to rewrite this for image...
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return compConf.treeDepth;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasModifier(ComponentModifier modifier) {
            return PackedComponentModifierSet.isSet(compConf.modifiers, modifier);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentModifierSet modifiers() {
            return compConf.modifiers();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return compConf.getName();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Component> parent() {
            ComponentNodeConfiguration cc = compConf.treeParent;
            return cc == null ? Optional.empty() : Optional.of(cc.adaptToComponent());
        }

        /** {@inheritDoc} */
        @Override
        public TreePath path() {
            return compConf.path();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentRelation relationTo(Component other) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Component resolve(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentStream stream(ComponentStream.Option... options) {
            return new PackedComponentStream(stream0(compConf, true, PackedComponentStreamOption.of(options)));
        }

        private Stream<Component> stream0(ComponentNodeConfiguration origin, boolean isRoot, PackedComponentStreamOption option) {
            // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
            children(); // lazy calc
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<ComponentAdaptor> c = (List) children();
            if (c != null && !c.isEmpty()) {
                if (option.processThisDeeper(origin, this.compConf)) {
                    Stream<Component> s = c.stream().flatMap(co -> co.stream0(origin, false, option));
                    return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
                }
                return Stream.empty();
            } else {
                return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
            }
        }
    }
}
