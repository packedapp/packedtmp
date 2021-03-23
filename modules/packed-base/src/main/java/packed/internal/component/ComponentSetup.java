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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.attribute.Attribute;
import app.packed.attribute.AttributeMap;
import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentAttributes;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.base.attribute.DefaultAttributeMap;
import packed.internal.base.attribute.PackedAttribute;
import packed.internal.base.attribute.PackedAttributeModel;
import packed.internal.base.attribute.PackedAttributeModel.Attt;
import packed.internal.component.InternalWirelet.SetComponentNameWirelet;
import packed.internal.component.source.SourceClassSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionModel;
import packed.internal.container.ExtensionSetup;
import packed.internal.util.ThrowableUtil;

/** A setup class for a component. Exposed to end-users as {@link ComponentConfigurationContext}. */
public final class ComponentSetup extends OpenTreeNode<ComponentSetup> implements ComponentConfigurationContext {

    /** Wirelets that was specified when creating the component. */
    @Nullable
    public final WireletWrapper wirelets;

    /** The modifiers of this component. */
    final int modifiers;

    /* *************** Setup **************** */

    /** The slot table this component is a part of. */
    public final SlotTableSetup table;

    /** The realm this component belongs to. */
    public final RealmSetup realm;

    /** The container setup if this component represents an container, otherwise null. */
    @Nullable
    public final ContainerSetup container;

    /** Any container this component is part of. A container is a member of it self. */
    @Nullable
    public final ContainerSetup memberOfContainer;

    /** The extension setup if this component represents an extension, otherwise null. */
    @Nullable
    public final ExtensionSetup extension;

    /** The class source setup if this component has a class source, otherwise null. */
    @Nullable
    public final SourceClassSetup source;

    /** The build this component is part of. */
    public final BuildSetup build;

    /**************** See how much of this we can get rid of. *****************/

    boolean isClosed = false;

    int nameState;

    Consumer<? super Component> onWire;

    static final int NAME_INITIALIZED_WITH_WIRELET = 1 << 18; // set atomically with DONE
    static final int NAME_SET = 1 << 17; // set atomically with ABNORMAL
    static final int NAME_GET = 1 << 16; // true if joiner waiting
    static final int NAME_GET_PATH = 1 << 15; // true if joiner waiting
    static final int NAME_CHILD_GOT_PATH = 1 << 14; // true if joiner waiting

    static final int NAME_GETSET_MASK = NAME_SET + NAME_GET + NAME_GET_PATH + NAME_CHILD_GOT_PATH;

    /**
     * Creates a new instance of this class
     * 
     * @param parent
     *            the parent of the component
     */
    ComponentSetup(BuildSetup build, RealmSetup realm, PackedComponentDriver<?> driver, @Nullable ComponentSetup parent, @Nullable WireletWrapper wirelets) {
        super(parent);
        this.extension = null; // Extensions use another constructor

        this.build = requireNonNull(build);
        this.wirelets = wirelets;
        // May initialize the component's name
        for (Wirelet w : wirelets.wirelets) {
            if (w instanceof InternalWirelet bw) {
                bw.firstPass(this);
            }
        }

        int mod = driver.modifiers;

        if (parent == null) {
            this.table = new SlotTableSetup(); // Root always needs a nodestore

            mod = mod | build.modifiers;
            // mod = PackedComponentModifierSet.add(mod, ComponentModifier.SYSTEM);
            if (build.modifiers().isContainerOld()) {
                // Is it a guest if we are analyzing??? Well we want the information...
                mod = PackedComponentModifierSet.add(mod, ComponentModifier.CONTAINEROLD);
            }
        } else {
            this.onWire = parent.onWire;
            this.table = driver.modifiers().isContainerOld() ? new SlotTableSetup() : parent.table;
        }
        this.modifiers = mod;

        // Setup Realm
        this.realm = requireNonNull(realm);
        ComponentSetup previous = realm.current;
        if (previous != null) {
            previous.fixCurrent();
        }
        realm.current = this;

        // Setup any container
        if (modifiers().isContainer()) {
            this.memberOfContainer = this.container = new ContainerSetup(this);
        } else {
            this.container = null;
            this.memberOfContainer = parent == null ? null : parent.memberOfContainer;
        }

        // Setup Guest
        if (modifiers().isContainerOld()) {
            table.reserve(); // reserve a slot to an instance of PackedGuest
        }

        // Setup any source
        if (modifiers().isSource()) {
            this.source = SourceClassSetup.create(this, driver);
        } else {
            this.source = null;
        }

        // Set a default name if up default name
        if (name == null) {
            setName0(null);
        }
    }

    void fixCurrent() {
        if (name == null) {
            setName(null);
        }
        if (onWire != null) {
            onWire.accept(adaptor());
        }
        // run onWiret
        // finalize name
    }

    /**
     * Create a new node representing an extension.
     * 
     * @param parent
     *            the parent (container) of the extension
     * @param extensionModel
     *            the extension model
     */
    public ComponentSetup(ComponentSetup parent, ExtensionModel extensionModel) {
        super(parent);
        this.build = parent.build;
        this.container = null;
        this.memberOfContainer = parent.container;
        this.extension = new ExtensionSetup(this, extensionModel);
        this.modifiers = PackedComponentModifierSet.I_EXTENSION;
        this.realm = new RealmSetup(extensionModel);
        this.realm.current = this; // IDK Den er jo ikke runtime...
        this.table = parent.table;
        this.source = null;
        this.wirelets = null;
        setName0(null /* model.nameComponent */); // setName0(String) does not work currently
    }

    /**
     * Returns a {@link Component} adaptor of this node.
     * 
     * @return a component adaptor
     */
    public Component adaptor() {
        return new ComponentAdaptor(this);
    }

    /** {@inheritDoc} */
    @Override
    public <T> void setRuntimeAttribute(Attribute<T> attribute, T value) {
        requireNonNull(attribute, "attribute is null");
        requireNonNull(value, "value is null");
        // check realm.open + attribute.write
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
            dam.addValue(ComponentAttributes.EXTENSION_MEMBER, extension.extensionClass());
            PackedAttributeModel pam = extension.model().attributes();
            if (pam != null) {
                for (Entry<PackedAttribute<?>, Attt> e : pam.attributeTypes.entrySet()) {
                    Extension ex = extension.extensionInstance();
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

        if (PackedComponentModifierSet.isSet(modifiers, ComponentModifier.APPLICATION)) {
            PackedApplicationDriver<?> pac = build().applicationDriver();
            dam.addValue(ComponentAttributes.SHELL_TYPE, pac.artifactRawType());
        }
        return dam;
    }

    /** {@inheritDoc} */
    @Override
    public BuildSetup build() {
        return build;
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (isClosed) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    /**
     * Closes the realm that this belongs component belongs to.
     * <p>
     * This method must only be called on a realms root component (we do not check explicitly this)
     * 
     * @see ComponentSetup#link(Assembly, Wirelet...)
     * @see BuildSetup#buildFromAssembly(PackedApplicationDriver, Assembly, Wirelet[], boolean, boolean)
     * @see BuildSetup#buildFromComposer(PackedApplicationDriver, PackedComponentDriver, java.util.function.Function,
     *      Consumer, Wirelet...)
     */
    void realmClose() {
        if (realm.current != null) {
            realm.current.fixCurrent();
        }
        realmClose0(realm);
    }

    /**
     * Called whenever a realm is closed on the top component in the realm.
     * 
     * @param realm
     *            the realm that was closed.
     */
    private void realmClose0(RealmSetup realm) {
        // Closes all components in the same realm depth first
        for (ComponentSetup component = treeFirstChild; component != null; component = component.treeNextSibling) {
            // child components with a different realm, is either:
            // in an another user realm that already been closed
            // in an extension realm that is closed in container.close
            if (component.realm == realm) {
                component.realmClose0(realm);
            }
        }
        // If this component represents container close the container
        if (container != null) {
            container.close(table);
        }
        isClosed = true;
    }

    /**
     * Returns the container this component is a part of. Or null if this component is the top level container.
     * 
     * @return the container this component is a part of
     */
    @Nullable
    public ContainerSetup getMemberOfContainer() {
        return memberOfContainer;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        // Only update with NAME_GET if no prev set/get op
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET;
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public Component link(Assembly<?> assembly, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        PackedComponentDriver<?> driver = AssemblyHelper.getDriver(assembly);

        // Create a wirelet wrapper from the specified wirelets
        WireletWrapper ww = WireletWrapper.forComponent(driver, wirelets);

        // If this component is an extension, we add it to the extension's container instead of the extension
        // itself, as the extension component is not retained at runtime
        ComponentSetup parent = extension == null ? this : treeParent; // treeParent is always a container if extension!=null

        // Create a new component and a new realm
        ComponentSetup component = new ComponentSetup(build, new RealmSetup(assembly), driver, parent, ww);

        // Invoke Assembly::build
        AssemblyHelper.invokeBuild(assembly, driver.toConfiguration(component));

        // Closes the the linked realm, no further configuration of it is possible after Assembly::build has been invoked
        component.realmClose();

        return new ComponentAdaptor(this);
    }

    /** {@inheritDoc} */
    @Override
    public PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    @Nullable
    public ComponentSetup getParent() {
        return treeParent;
    }

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        int anyPathMask = NAME_GET_PATH + NAME_CHILD_GOT_PATH;
        if ((nameState & anyPathMask) != 0) {
            ComponentSetup p = treeParent;
            while (p != null && ((p.nameState & anyPathMask) == 0)) {
                p.nameState = (p.nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
            }
        }
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
        return PackedTreePath.of(this); // show we weak intern them????
    }

    public void checkCurrent() {
        if (realm.current != this) {
            throw new IllegalStateException("This operation must be called immediately after wiring of the component");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        // First lets check the name is valid
        SetComponentNameWirelet.checkName(name);
        int s = nameState;

        checkConfigurable();
        checkCurrent();

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
            if (nameState == NAME_INITIALIZED_WITH_WIRELET) {
                n = name;
            }
        }

        boolean isFree = false;

        if (n == null) {
            if (source != null) {
                n = source.model.simpleName();
            } else if (container != null) {
                // I think try and move some of this to ComponentNameWirelet
                @Nullable
                Class<?> source = realm.realmType();
                if (Assembly.class.isAssignableFrom(source)) {
                    String nnn = source.getSimpleName();
                    if (nnn.length() > 8 && nnn.endsWith("Assembly")) {
                        nnn = nnn.substring(0, nnn.length() - 8);
                    }
                    if (nnn.length() > 0) {
                        // checkName, if not just App
                        // TODO need prefix
                        n = nnn;
                    }
                    if (nnn.length() == 0) {
                        n = "Assembly";
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
    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        PackedComponentDriver<C> d = (PackedComponentDriver<C>) requireNonNull(driver, "driver is null");
        WireletWrapper wp = WireletWrapper.forComponent(d, wirelets);
        // ConfigSite configSite = captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);

        // When an extension adds new components they are added to the container (the extension's parent)
        // Instead of the extension, because the extension itself is removed at runtime.
        ComponentSetup parent = extension == null ? this : treeParent;
        ComponentSetup compConf = new ComponentSetup(build, realm, d, parent, wp);

        // We only close the component if linking a assembly (new realm)
        return d.toConfiguration(compConf);
    }

    // This should only be called by special methods
    // We just take the lookup to make sure caller think twice before calling this method.
    public static ComponentSetup unadapt(Lookup caller, Component component) {
        if (component instanceof ComponentAdaptor ca) {
            return ca.compConf;
        }
        throw new IllegalStateException("This method must be called before a component is instantiated");
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
    private void checkIsContainer() {
        if (container == null) {
            throw new UnsupportedOperationException(
                    "This method can only be called component that has the " + ComponentModifier.class.getSimpleName() + ".CONTAINER modifier set");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> containerExtensions() {
        checkIsContainer();
        return container.extensionView();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T containerUse(Class<T> extensionClass) {
        checkIsContainer();
        return container.useExtension(extensionClass);
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
        return (ExportedServiceConfiguration<T>) memberOfContainer.getServiceManagerOrCreate().exports().export(source.service);
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

    /** An adaptor of the {@link Component} interface from a {@link ComponentSetup}. */
    private static final class ComponentAdaptor implements Component {

        /** The component configuration to wrap. */
        private final ComponentSetup compConf;

        private ComponentAdaptor(ComponentSetup compConf) {
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
            return compConf.toList(ComponentSetup::adaptor);
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
            ComponentSetup cc = compConf.treeParent;
            return cc == null ? Optional.empty() : Optional.of(cc.adaptor());
        }

        /** {@inheritDoc} */
        @Override
        public NamespacePath path() {
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

        private Stream<Component> stream0(ComponentSetup origin, boolean isRoot, PackedComponentStreamOption option) {
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

///**
//* @param frame
//*            the frame to filter
//* @return whether or not to filter the frame
//*/
//private boolean captureStackFrameIgnoreFilter(StackFrame frame) {
//  Class<?> c = frame.getDeclaringClass();
//  // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract assembly der override configure()...
//  // Syntes bare vi filtrer app.packed.base modulet fra...
//  // Kan vi ikke checke om imod vores container source.
//
//  // ((PackedExtensionContext) context()).container().source
//  // Nah hvis man koere fra config er det jo fint....
//  // Fra config() paa en assembly er det fint...
//  // Fra alt andet ikke...
//
//  // Dvs ourContainerSource
//  return Extension.class.isAssignableFrom(c)
//          || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && Assembly.class.isAssignableFrom(c));
//}

///**
//* Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
//* not located on any subclasses of {@link Extension} or any class that implements
//* <p>
//* Invoking this method typically takes in the order of 1-2 microseconds.
//* <p>
//* If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
//* {@link ConfigSite#UNKNOWN}.
//* 
//* @param operation
//*            the operation
//* @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
//* @see StackWalker
//*/
//// TODO add stuff about we also ignore non-concrete container sources...
//ConfigSite captureStackFrame(String operation) {
//  // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
//  // to the extension class in order to simplify the filtering mechanism.
//
//  // Vi kan spoerge "if context.captureStackFrame() ...."
//
//  if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
//      return ConfigSite.UNKNOWN;
//  }
//  Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
//  return ConfigSite.UNKNOWN;
//  //return sf.isPresent() ? Config configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
//}
