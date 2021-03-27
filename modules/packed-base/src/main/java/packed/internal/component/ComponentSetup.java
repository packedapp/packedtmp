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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionModel;
import packed.internal.container.ExtensionSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;
import packed.internal.util.ThrowableUtil;

/** A setup class for a component. Exposed to end-users as {@link ComponentConfigurationContext}. */
public abstract class ComponentSetup {

    /** The application this component is a part of. */
    public final ApplicationSetup application;

    /** The build this component is part of. */
    public final BuildSetup build;

    /** Children of this node (lazily initialized) in insertion order. */
    @Nullable
    LinkedHashMap<String, ComponentSetup> children;

    /** The container this component is a part of. A container is a part of it self. */
    public final ContainerSetup container;

    /** The depth of the component in the tree. */
    protected final int depth;

    boolean isClosed;

    /** The modifiers of this component. */
    protected final int modifiers;

    /** The name of this node. */
    protected String name;

    /** Whether or not the name has been initialized via a wirelet, in which case it is not overridable by setName(). */
    protected boolean nameInitializedWithWirelet;

    /** An action that, if present, must be called whenever the component has been completely wired. */
    @Nullable
    protected Consumer<? super Component> onWire;

    /** The parent of this component, or null for a root component. */
    @Nullable
    protected final ComponentSetup parent;

    public final ConstantPoolSetup pool;

    /** The realm this component is a part of. */
    public final RealmSetup realm;

    /**
     * Creates a new instance of this class
     * 
     * @param parent
     *            the parent of the component
     */
    public ComponentSetup(BuildSetup build, ApplicationSetup application, RealmSetup realm, WireableComponentDriver<?> driver,
            @Nullable ComponentSetup parent) {
        // Update the current
        realm.newCurrent(this);

        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;

        this.build = requireNonNull(build);
        this.application = requireNonNull(application);
        this.container = this instanceof ContainerSetup container ? container : parent.container;
        this.realm = realm;

        // Various
        if (parent == null) {
            this.modifiers = build.modifiers | driver.modifiers;
            this.pool = application.constantPool;
        } else {
            this.modifiers = driver.modifiers;
            this.pool = driver.modifiers().hasRuntime() ? new ConstantPoolSetup() : parent.pool;
            this.onWire = parent.onWire;
        }

        // Setup Runtime
        if (modifiers().hasRuntime()) {
            pool.reserve(); // reserve a slot to an instance of PackedApplicationRuntime
        }
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
        this.parent = container;
        this.depth = container.depth + 1;

        this.build = container.build;
        this.application = container.application;
        this.container = container;
        this.realm = new RealmSetup(model, this);

        this.pool = container.pool;
        this.modifiers = PackedComponentModifierSet.I_EXTENSION;
        this.onWire = container.onWire;
    }

    /**
     * Returns a {@link Component} adaptor of this node.
     * 
     * @return a component adaptor
     */
    public final Component adaptor() {
        return new ComponentAdaptor(this);
    }

    AttributeMap attributes() {
        // Det er ikke super vigtigt at den her er hurtig paa configurations tidspunktet...
        // Maaske er det simpelthen et view...
        // Hvor vi lazily fx calculere EntrySet (og gemmer i et felt)
        DefaultAttributeMap dam = new DefaultAttributeMap();
        attributesAdd(dam);
        return dam;
    }

    protected void attributesAdd(DefaultAttributeMap dam) {}

    public final BuildSetup build() {
        return build;
    }

    public void checkConfigurable() {
        realm.checkOpen();
        if (isClosed) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    public final void checkIsWiring() {
        if (realm.current() != this) {
            throw new IllegalStateException("This operation must be called immediately after wiring of the component");
        }
    }

    final int childrenCount() {
        return children == null ? 0 : children.size();
    }

    final void finishWiring() {
        if (onWire != null) {
            onWire.accept(adaptor());
        }
    }

    protected final void initializeName(String name) {
        String n = name;
        if (parent != null) {
            Map<String, ComponentSetup> c = parent.children;
            if (c == null) {
                c = parent.children = new LinkedHashMap<>();
                c.put(name, this);
            } else {
                int counter = 1;
                while (c.putIfAbsent(n, this) != null) {
                    n = name + counter++; // maybe store some kind of fallback map on build.. for those that want to test adding 1 mil components
                }
            }
        }
        this.name = n;
    }

    public final Component link(Assembly<?> assembly, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        WireableComponentDriver<?> driver = WireableComponentDriver.getDriver(assembly);

        // Check that the realm this component is a part of is still open
        realm.wireNew();

        // If this component is an extension, we link to extension's container. As the extension itself is not available at
        // runtime
        ComponentSetup linkTo = this instanceof ExtensionSetup ? parent : this;

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

    public final PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    public final NamespacePath path() {
        return PackedTreePath.of(this); // show we weak intern them????
    }

    public final <T> void setRuntimeAttribute(Attribute<T> attribute, T value) {
        requireNonNull(attribute, "attribute is null");
        requireNonNull(value, "value is null");
        // check realm.open + attribute.write
    }

    public final <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        WireableComponentDriver<C> pcd = (WireableComponentDriver<C>) requireNonNull(driver, "driver is null");

        // Check that this component's is still open
        realm.wireNew();

        // If this component is an extension, we wire to the container the extension is part of
        ComponentSetup wireTo = this instanceof ExtensionSetup ? parent : this;

        // Create the new component
        WireableComponentSetup component = pcd.newComponent(build, application, realm, wireTo, wirelets);
        
        // Return a component configuration to the user
        return pcd.toConfiguration(component);
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

    // This should only be called by special methods
    // We just take the lookup to make sure caller think twice before calling this method.
    public static ComponentSetup unadapt(Lookup caller, Component component) {
        if (component instanceof ComponentAdaptor ca) {
            return ca.component;
        }
        throw new IllegalStateException("This method must be called before a component is instantiated");
    }

    /** An adaptor of the {@link Component} interface for a {@link ComponentSetup}. */
    private record ComponentAdaptor(ComponentSetup component) implements Component {

        /** {@inheritDoc} */
        @Override
        public AttributeMap attributes() {
            return component.attributes();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            return childrenToList(ComponentSetup::adaptor);
        }

        @SuppressWarnings("unchecked")
        private <S> List<S> childrenToList(Function<ComponentSetup, S> mapper) {
            requireNonNull(mapper, "mapper is null");
            LinkedHashMap<String, ComponentSetup> c = component.children;
            if (c == null) {
                return List.of();
            } else {
                List.copyOf(c.values());
            }
            int size = c == null ? 0 : c.size();
            if (size == 0) {
                return List.of();
            } else {
                Object[] o = new Object[size];
                int index = 0;
                for (ComponentSetup child : c.values()) {
                    o[index++] = mapper.apply(child);
                }
                return (List<S>) List.of(o);
            }
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return component.depth;
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
            return component.name;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Component> parent() {
            ComponentSetup cc = component.parent;
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
            if (component.children != null) {
                if (component.children.containsKey(path)) {
                    return component.children.get(path.toString()).adaptor();
                }
            }
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
