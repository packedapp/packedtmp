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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.attribute.Attribute;
import app.packed.attribute.AttributeMap;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.BuildSetup;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.component.source.ClassSourceSetup;
import packed.internal.component.source.SourceComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionModel;
import packed.internal.container.ExtensionSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;
import packed.internal.util.ThrowableUtil;

/** A setup class for a component. Exposed to end-users as {@link ComponentConfigurationContext}. */
public abstract class ComponentSetup extends OpenTreeNode<ComponentSetup> {

    /** The application this component is a part of. */
    public final ApplicationSetup application;

    /** The build this component is part of. */
    public final BuildSetup build;

    /** The container this component is a part of. A container is a part of it self. */
    public final ContainerSetup container;

    /** The modifiers of this component. */
    protected final int modifiers;

    public final ConstantPoolSetup pool;

    /** The realm this component is a part of. */
    public final RealmSetup realm;

    /**************** See how much of this we can get rid of. *****************/

    boolean isClosed = false;

    boolean nameInitializedWithWirelet;

    int nameState;

    protected Consumer<? super Component> onWire;

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
    public ComponentSetup(BuildSetup build, ApplicationSetup application, RealmSetup realm, WireableComponentDriver<?> driver,
            @Nullable ComponentSetup parent) {
        super(parent);
        this.application = requireNonNull(application);
        this.build = requireNonNull(build);

        // Setup Realm

        // Various
        if (parent == null) {
            this.modifiers = build.modifiers | driver.modifiers;
            this.pool = application.constantPool;
        } else {
            this.modifiers = driver.modifiers;
            this.pool = driver.modifiers().hasRuntime() ? new ConstantPoolSetup() : parent.pool;
            this.onWire = parent.onWire;
        }

        // Setup container
        this.container = this instanceof ContainerSetup container ? container : parent.container;

        // Setup Runtime
        if (modifiers().hasRuntime()) {
            pool.reserve(); // reserve a slot to an instance of PackedApplicationRuntime
        }

        // finally update the realm
        this.realm = realm;
        realm.updateCurrent(this);
    }

    /**
     * Constructor used by {@link ExtensionSetup}.
     * 
     * @param container
     *            the extension's container (parent)
     * @param extensionModel
     *            a model of the extension
     */
    protected ComponentSetup(ContainerSetup container, ExtensionModel model) {
        super(container);
        this.application = container.application;
        this.build = container.build;
        this.pool = container.pool;
        this.container = container;
        this.modifiers = PackedComponentModifierSet.I_EXTENSION;
        this.realm = new RealmSetup(model, this);
        this.onWire = container.onWire;
        container.addChildFinalName(this, model.nameComponent);
    }

    /**
     * Returns a {@link Component} adaptor of this node.
     * 
     * @return a component adaptor
     */
    public Component adaptor() {
        return new ComponentAdaptor(this);
    }

    // runtime???
    protected void addAttributes(DefaultAttributeMap dam) {}

    AttributeMap attributes() {
        // Det er ikke super vigtigt at den her er hurtig paa configurations tidspunktet...
        // Maaske er det simpelthen et view...
        // Hvor vi lazily fx calculere EntrySet (og gemmer i et felt)
        DefaultAttributeMap dam = new DefaultAttributeMap();
        addAttributes(dam);
        return dam;
    }

    public BuildSetup build() {
        return build;
    }

    public void checkConfigurable() {
        if (isClosed) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    public void checkCurrent() {
        if (realm.current() != this) {
            throw new IllegalStateException("This operation must be called immediately after wiring of the component");
        }
    }

    void fixCurrent() {
        if (name == null) {
            setName(null);
        }
        if (onWire != null) {
            onWire.accept(adaptor());
        }
        isClosed = true;
        // run onWiret
        // finalize name
    }

    public String getName() {
        // Only update with NAME_GET if no prev set/get op
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET;
        return name;
    }

    @Nullable
    public ComponentSetup getParent() {
        return treeParent;
    }

    public final Component link(Assembly<?> assembly, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        WireableComponentDriver<?> driver = WireableComponentDriver.getDriver(assembly);

        // Check that the realm this component is a part of is still open
        realm.checkOpen();

        // If this component is an extension, we link to extension's container. As the extension itself is not available at
        // runtime
        ComponentSetup linkTo = this instanceof ExtensionSetup ? treeParent : this;

        // Create a new realm for the assembly
        RealmSetup realm = new RealmSetup(assembly);

        // Create a new component and a new realm
        WireableComponentSetup component = driver.newComponent(build, application, realm, linkTo, wirelets);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration configuration = driver.toConfiguration(component);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the newly create realm
        realm.close(component);

        return new ComponentAdaptor(component);
    }

    public PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

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

    public void setName(String name) {
        // First lets check the name is valid
        checkComponentName(name);
        checkCurrent();
        int s = nameState;

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
        if (name.equals(this.name)) {
            return;
        }

        if (nameInitializedWithWirelet) {
            return;
        }

        setName0(name, null);
    }

    /**
     * Checks the name of the component.
     * 
     * @param name
     *            the name to check
     * @return the name if valid
     */
    public static String checkComponentName(String name) {
        requireNonNull(name, "name is null");
        if (name != null) {

        }
        return name;
    }

    protected void setName0(String newName, ExtensionModel extensionModel) {
        String n = newName;
        if (newName == null) {
            if (nameInitializedWithWirelet) {
                n = name;
            }
        }

        boolean isFree = false;

        if (n == null) {
            ClassSourceSetup src = this instanceof SourceComponentSetup bcs ? bcs.source : null;
            if (src != null) {
                n = src.model.simpleName();
            } else if (this instanceof ContainerSetup container) {
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
                // We can keep the counter in Build <ComponentSetup(parent), counter)
                // In this way we do not need to main a map for every component.

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
                    treeParent.treeChildren = new LinkedHashMap<>();
                } else {
                }
                treeParent.treeChildren.put(n, this);
            }
        }
        name = n;
    }

    public <T> void setRuntimeAttribute(Attribute<T> attribute, T value) {
        requireNonNull(attribute, "attribute is null");
        requireNonNull(value, "value is null");
        // check realm.open + attribute.write
    }

    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        WireableComponentDriver<C> pcd = (WireableComponentDriver<C>) requireNonNull(driver, "driver is null");

        // Check that the realm this component is a part of is still open
        realm.checkOpen();

        // If this component is an extension, we wire to the container the extension is part of
        ComponentSetup wireTo = this instanceof ExtensionSetup ? treeParent : this;

        // Create the new component
        WireableComponentSetup component = pcd.newComponent(build, application, realm, wireTo, wirelets);

        // Return a component configuration to the user
        return pcd.toConfiguration(component);
    }

    // This should only be called by special methods
    // We just take the lookup to make sure caller think twice before calling this method.
    public static ComponentSetup unadapt(Lookup caller, Component component) {
        if (component instanceof ComponentAdaptor ca) {
            return ca.component;
        }
        throw new IllegalStateException("This method must be called before a component is instantiated");
    }

    /** An adaptor of the {@link Component} interface from a {@link ComponentSetup}. */
    private record ComponentAdaptor(ComponentSetup component) implements Component {

        /** {@inheritDoc} */
        @Override
        public AttributeMap attributes() {
            return component.attributes();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            return component.toList(ComponentSetup::adaptor);
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return component.treeDepth;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasModifier(ComponentModifier modifier) {
            return PackedComponentModifierSet.isSet(component.modifiers, modifier);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentModifierSet modifiers() {
            return component.modifiers();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return component.getName();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Component> parent() {
            ComponentSetup cc = component.treeParent;
            return cc == null ? Optional.empty() : Optional.of(cc.adaptor());
        }

        /** {@inheritDoc} */
        @Override
        public NamespacePath path() {
            return component.path();
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
            return new PackedComponentStream(stream0(component, true, PackedComponentStreamOption.of(options)));
        }

        private Stream<Component> stream0(ComponentSetup origin, boolean isRoot, PackedComponentStreamOption option) {
            // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
            children(); // lazy calc
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<ComponentAdaptor> c = (List) children();
            if (c != null && !c.isEmpty()) {
                if (option.processThisDeeper(origin, this.component)) {
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
