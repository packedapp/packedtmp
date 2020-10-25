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
import app.packed.cube.Extension;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.base.attribute.DefaultAttributeMap;
import packed.internal.base.attribute.PackedAttribute;
import packed.internal.base.attribute.ProvidableAttributeModel;
import packed.internal.base.attribute.ProvidableAttributeModel.Attt;
import packed.internal.component.source.RealmBuild;
import packed.internal.component.source.SourceBuild;
import packed.internal.component.wirelet.InternalWirelet.ComponentNameWirelet;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.config.ConfigSiteInjectOperations;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.cube.CubeBuild;
import packed.internal.cube.ExtensionBuild;
import packed.internal.cube.ExtensionModel;
import packed.internal.util.ThrowableUtil;

/** The build time representation of a component. */
public final class ComponentBuild extends OpenTreeNode<ComponentBuild> implements ComponentConfigurationContext {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** Any wirelets that was specified by the user when creating this configuration. */
    @Nullable
    public final WireletPack wirelets;

    /** The modifiers of this configuration. */
    final int modifiers;

    /* *************** ASSEMBLIES **************** */

    /** The region this component is a part of. */
    public final RegionBuild region;

    /** The realm this component is a part of. */
    public final RealmBuild realm;

    /** The container, if source Any container this component is part of. A container is part of it self. */
    @Nullable
    public final CubeBuild cube;

    /** Any container this component is part of. A container is part of it self. */
    @Nullable
    public final CubeBuild memberOfCube;

    /** Any extension that is attached to this component. */
    @Nullable
    public final ExtensionBuild extension;

    /** Any source that is attached to this component. */
    @Nullable
    public final SourceBuild source;

    /**************** See how much of this we can get rid of. *****************/

    /** The configuration site of this component. */
    private final ConfigSite configSite;

    boolean isClosed = false;

    int nameState;

    private static final int NAME_INITIALIZED_WITH_WIRELET = 1 << 18; // set atomically with DONE
    private static final int NAME_SET = 1 << 17; // set atomically with ABNORMAL
    private static final int NAME_GET = 1 << 16; // true if joiner waiting
    private static final int NAME_GET_PATH = 1 << 15; // true if joiner waiting
    private static final int NAME_CHILD_GOT_PATH = 1 << 14; // true if joiner waiting

    private static final int NAME_GETSET_MASK = NAME_SET + NAME_GET + NAME_GET_PATH + NAME_CHILD_GOT_PATH;

    final PackedBuildContext build;

    /**
     * Creates a new instance of this class
     * 
     * @param configSite
     *            the configuration site of the component
     * @param parent
     *            the parent of the component
     */
    ComponentBuild(PackedBuildContext build, RealmBuild realm, PackedComponentDriver<?> driver, ConfigSite configSite,
            @Nullable ComponentBuild parent, @Nullable WireletPack wirelets) {
        super(parent);
        this.configSite = requireNonNull(configSite);
        this.extension = null; // Extensions use another constructor

        this.build = requireNonNull(build);
        this.wirelets = wirelets;
        int mod = driver.modifiers;
        if (parent == null) {
            this.region = new RegionBuild(); // Root always needs a nodestore

            mod = mod | build.modifiers;
            mod = PackedComponentModifierSet.add(mod, ComponentModifier.SYSTEM);
            if (build.modifiers().isGuest()) {
                // Is it a guest if we are analyzing??? Well we want the information...
                mod = PackedComponentModifierSet.add(mod, ComponentModifier.CONTAINER);
            }
        } else {
            this.region = driver.modifiers().isGuest() ? new RegionBuild() : parent.region;
        }
        this.modifiers = mod;

        // Setup Realm
        this.realm = requireNonNull(realm);

        // Setup Container
        if (modifiers().isContainer()) {
            this.memberOfCube = this.cube = new CubeBuild(this);
        } else {
            this.cube = null;
            this.memberOfCube = parent == null ? null : parent.memberOfCube;
        }

        // Setup Guest
        if (modifiers().isGuest()) {
            region.reserve(); // reserve a slot to an instance of PackedGuest
        }

        // Setup Source
        if (modifiers().isSource()) {
            this.source = SourceBuild.create(this, driver);
        } else {
            this.source = null;
        }

        // Setup default name
        setName0(null);
    }

    /**
     * Create a new node representing an extension.
     * 
     * @param parent
     *            the parent (container) of the extension
     * @param model
     *            the extension model
     */
    public ComponentBuild(ComponentBuild parent, ExtensionModel model) {
        super(parent);
        this.build = parent.build;
        this.configSite = parent.configSite();
        this.cube = null;
        this.memberOfCube = parent.cube;
        this.extension = new ExtensionBuild(this, model);
        this.modifiers = PackedComponentModifierSet.I_EXTENSION;
        this.realm = new RealmBuild(model.type());
        this.region = parent.region;
        this.source = null;
        this.wirelets = null;
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
            dam.addValue(ComponentAttributes.SOURCE_TYPE, source.model.type);
        }

        if (extension != null) {
            dam.addValue(ComponentAttributes.EXTENSION_MEMBER, extension.extensionType());
            ProvidableAttributeModel pam = extension.model().attributes();
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
            PackedShellDriver<?> psd = (PackedShellDriver<?>) assembly().shellDriver();
            dam.addValue(ComponentAttributes.SHELL_TYPE, psd.shellRawType());
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
    public PackedBuildContext assembly() {
        return build;
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (isClosed) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    public void close() {
        onRealmClose(realm);
    }

    /**
     * Called whenever a realm is closed on the top component in the realm.
     * 
     * @param realm
     *            the realm that was closed.
     */
    public void onRealmClose(RealmBuild realm) {
        // Closes all components in the same realm depth first
        for (ComponentBuild compConf = treeFirstChild; compConf != null; compConf = compConf.treeNextSibling) {
            // child components with a different realm, has either already been closed, or will be closed elsewhere
            if (compConf.realm == realm) {
                compConf.onRealmClose(realm);
            }
        }

        if (cube != null) {
            cube.close(region);
        }
        isClosed = true;
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
    public CubeBuild getMemberOfContainer() {
        return memberOfCube;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        // Only update with NAME_GET if no prev set/get op
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET;
        return name;
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

        WireletPack wp = WireletPack.from(driver, wirelets);

        // ConfigSite cs = ConfigSiteSupport.captureStackFrame(configSite(), ConfigSiteInjectOperations.INJECTOR_OF);
        ConfigSite cs = ConfigSite.UNKNOWN;

        // If this component is an extension, we add it to the extension's container instead of the extension
        // itself, as the extension component is not retained at runtime
        ComponentBuild parent = extension == null ? this : treeParent;

        // Create the new component
        ComponentBuild compConf = new ComponentBuild(build, new RealmBuild(bundle.getClass()), driver, cs, parent, wp);

        // Invoke Bundle::configure
        BundleHelper.configure(bundle, driver.toConfiguration(compConf));

        // Close the the linked realm, no further configuration of it is possible after Bundle::configure has been invoked
        compConf.close();
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    @Nullable
    public ComponentBuild getParent() {
        return treeParent;
    }

    /** {@inheritDoc} */
    @Override
    public TreePath path() {
        int anyPathMask = NAME_GET_PATH + NAME_CHILD_GOT_PATH;
        if ((nameState & anyPathMask) != 0) {
            ComponentBuild p = treeParent;
            while (p != null && ((p.nameState & anyPathMask) == 0)) {
                p.nameState = (p.nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
            }
        }
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
        return PackedTreePath.of(this); // show we weak intern them????
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
            } else if (cube != null) {
                // I think try and move some of this to ComponentNameWirelet
                @Nullable
                Class<?> source = realm.realmType();
                if (Bundle.class.isAssignableFrom(source)) {
                    String nnn = source.getSimpleName();
                    if (nnn.length() > 6 && nnn.endsWith("Bundle")) {
                        nnn = nnn.substring(0, nnn.length() - 6);
                    }
                    if (nnn.length() > 0) {
                        // checkName, if not just App
                        // TODO need prefix
                        n = nnn;
                    }
                    if (nnn.length() == 0) {
                        n = "Container";
                    }
                }
                // TODO think it should be named Artifact type, for example, app, injector, ...
            } else if (extension != null) {
                n = extension.model().nameComponent;
            }
            if (n == null) {
                n = "Unknown";
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

        // When an extension adds new components they are added to the container (the extension's parent)
        // Instead of the extension, because the extension itself is removed at runtime.
        ComponentBuild parent = extension == null ? this : treeParent;
        ComponentBuild compConf = new ComponentBuild(build, realm, d, configSite, parent, wp);

        // We only close the component if linking a bundle (new realm)
        return d.toConfiguration(compConf);
    }

    // This should only be called by special methods
    // We just take the lookup to make sure caller think twice before calling this method.
    public static ComponentBuild unadapt(Lookup caller, Component component) {
        if (!(component instanceof ComponentAdaptor)) {
            throw new IllegalStateException("This method must be called before a component is instantiated");
        }
        ComponentAdaptor cc = (ComponentAdaptor) component;
        return cc.compConf;
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
        if (cube == null) {
            throw new UnsupportedOperationException(
                    "This method can only be called component that has the " + ComponentModifier.class.getSimpleName() + ".CONTAINER modifier set");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> containerExtensions() {
        checkHasContainer();
        return cube.extensionView();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T containerUse(Class<T> extensionType) {
        checkHasContainer();
        return cube.useExtension(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public void sourceProvide() {
        checkConfigurable();
        checkHasSource();
        source.provide(this);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Key<?>> sourceProvideAsKey() {
        checkHasSource();
        return source.service == null ? Optional.empty() : Optional.of(source.service.key());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) memberOfCube.getServiceManagerOrCreate().exports().export(source.service,
                captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    @Override
    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        if (source == null) {
            throw new UnsupportedOperationException();
        }
        source.provide(this).as(key);
    }

    /** An adaptor of the {@link Component} interface from a {@link ComponentBuild}. */
    private static final class ComponentAdaptor implements Component {

        /** The component configuration to wrap. */
        private final ComponentBuild compConf;

        private ComponentAdaptor(ComponentBuild compConf) {
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
            return compConf.toList(ComponentBuild::adaptToComponent);
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
            ComponentBuild cc = compConf.treeParent;
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

        private Stream<Component> stream0(ComponentBuild origin, boolean isRoot, PackedComponentStreamOption option) {
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