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

import app.packed.application.Application;
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
import app.packed.component.ComponentScope;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import app.packed.container.ExtensionConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.BuildSetup;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionModel;
import packed.internal.container.ExtensionSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;
import packed.internal.util.ThrowableUtil;

/** Build-time configuration of a component. Exposed to end-users as {@link ComponentConfigurationContext}. */
public abstract class ComponentSetup {

    /** The application this component is a part of. */
    public final ApplicationSetup application;

    /** Children of this node (lazily initialized) in insertion order. */
    @Nullable
    LinkedHashMap<String, ComponentSetup> children;

    /** The container this component is a part of. A container is a part of it self. */
    public final ContainerSetup container;

    /** The depth of the component in the tree. */
    protected final int depth;

    /** The modifiers of this component. */
    protected final int modifiers;

    /** The name of this component. */
    protected String name;

    /** Whether or not the name has been initialized via a wirelet, in which case it cannot be overridden by setName(). */
    boolean nameInitializedWithWirelet;

    /** An action that, if present, must be called whenever the component has been completely wired. */
    @Nullable
    protected Consumer<? super Component> onWire;

    /** The parent of this component, or null for a root component. */
    @Nullable
    protected final ComponentSetup parent;

    /** This component's constant pool. */
    public final ConstantPoolSetup pool;

    /** The realm this component is a part of. */
    public final RealmSetup realm;

    /**
     * Create a new component.
     * 
     * @param application
     *            the application this component is a part of
     * @param realm
     *            the realm this component is part of
     * @param driver
     *            the driver of the component
     * @param parent
     *            any parent this component might have
     */
    ComponentSetup(ApplicationSetup application, RealmSetup realm, WireableComponentDriver<?> driver, @Nullable ComponentSetup parent) {
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;

        this.application = requireNonNull(application);
        this.realm = requireNonNull(realm);
        this.container = this instanceof ContainerSetup container ? container : parent.container;

        // Various
        if (/* is root container */ parent == null) {
            this.modifiers = application.build.modifiers | driver.modifiers;
            this.pool = application.constantPool;
        } else {
            this.modifiers = driver.modifiers;
            this.pool = driver.modifiers().hasRuntime() ? new ConstantPoolSetup() : parent.pool;
            this.onWire = parent.onWire;
        }
    }

    /**
     * Create a new extension via {@link ExtensionSetup}.
     * 
     * @param container
     *            the extension's container (parent)
     * @param extensionModel
     *            a model of the extension
     */
    protected ComponentSetup(ContainerSetup container, ExtensionModel model) {
        this.parent = container;
        this.depth = container.depth + 1;

        this.application = container.application;
        this.container = container;
        this.realm = new RealmSetup(model, this);

        this.modifiers = PackedComponentModifierSet.I_EXTENSION;
        this.pool = container.pool;
        this.onWire = container.onWire;
        // Cannot use wirelets with extensions. So the name is final
        initializeNameWithPrefix(model.nameComponent);
    }

    /**
     * Returns a {@link Component} adaptor of this node.
     * 
     * @return a component adaptor
     */
    public final Component adaptor() {
        return new ComponentAdaptor(this);
    }

    final AttributeMap attributes() {
        // Det er ikke super vigtigt at den her er hurtig paa configurations tidspunktet...
        // Maaske er det simpelthen et view...
        // Hvor vi lazily fx calculere EntrySet (og gemmer i et felt)
        DefaultAttributeMap dam = new DefaultAttributeMap();
        attributesAdd(dam);
        return dam;
    }

    protected void attributesAdd(DefaultAttributeMap dam) {}

    public final BuildSetup build() {
        return application.build;
    }

    public final void checkIsWiring() {
        if (realm.current() != this) {
            throw new IllegalStateException("This operation must be called immediately after wiring of the component");
        }
    }

    public void checkOpen() {
        realm.checkOpen();
    }

    protected final void initializeNameWithPrefix(String name) {
        String n = name;
        if (parent != null) {
            LinkedHashMap<String, ComponentSetup> c = parent.children;
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

    /**
     * Tests that this component is in the same specified scope as other.
     * 
     * @param scope
     *            the scope to test
     * @param other
     *            the other component to test
     * @return true if in the same scope, otherwise false
     */
    public final boolean isInSame(ComponentScope scope, ComponentSetup other) {
        requireNonNull(scope, "scope is null");
        requireNonNull(other, "other is null");
        return switch (scope) {
        case APPLICATION -> application == other.application;
        case BUILD -> application.build == other.application.build;
        case COMPONENT -> this == other;
        case CONTAINER -> container == other.container;
        case NAMESPACE -> application.build.namespace == other.application.build.namespace;
        };
    }

    /**
     * Links a new assembly.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return the component that was linked
     * @see ComponentConfigurationContext#link(Assembly, Wirelet...)
     * @see ExtensionConfiguration#link(Assembly, Wirelet...)
     */
    public final Component link(Assembly<?> assembly, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        WireableComponentDriver<?> driver = WireableComponentDriver.getDriver(assembly);

        // If this component is an extension, we link to the extension's container.
        // As the extension itself is not present at runtime
        ComponentSetup linkTo = this instanceof ExtensionSetup ? parent : this;

        // Create the new realm that should be used for linking
        RealmSetup newRealm = realm.link(driver, linkTo, assembly, wirelets);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration configuration = driver.toConfiguration(newRealm.root);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the newly create realm
        newRealm.close();

        return newRealm.root.adaptor();
    }

    public final PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    protected final void onWired() {
        if (onWire != null) {
            onWire.accept(adaptor());
        }
    }

    /** {@return the path of this component} */
    public final NamespacePath path() {
        return PackedTreePath.of(this);
    }

    public final <T> void setRuntimeAttribute(Attribute<T> attribute, T value) {
        requireNonNull(attribute, "attribute is null");
        requireNonNull(value, "value is null");
        // check realm.open + attribute.write
    }

    public final <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        WireableComponentDriver<C> wcd = (WireableComponentDriver<C>) requireNonNull(driver, "driver is null");

        // If this component is an extension, we wire to the container the extension is a part of
        ComponentSetup wireTo = this instanceof ExtensionSetup ? parent : this;

        // Wire a new component
        WireableComponentSetup component = realm.wire(wcd, wireTo, wirelets);

        // Return a component configuration to the user
        return wcd.toConfiguration(component);
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
            LinkedHashMap<String, ComponentSetup> c = component.children;
            return c == null ? List.of() : c.values().stream().map(e -> e.adaptor()).toList();
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
            requireNonNull(other, "other is null");
            return ComponentSetupRelation.of(component, ((ComponentAdaptor) other).component);
        }

        /** {@inheritDoc} */
        @Override
        public Component resolve(CharSequence path) {
            LinkedHashMap<String, ComponentSetup> c = component.children;
            if (c != null) {
                ComponentSetup cs = c.get(path.toString());
                if (cs != null) {
                    return cs.adaptor();
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
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<ComponentAdaptor> c = (List) children();
            if (c != null && !c.isEmpty()) {
                if (option.processThisDeeper(origin, component)) {
                    Stream<Component> s = c.stream().flatMap(co -> co.stream0(origin, false, option));
                    return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
                }
                return Stream.empty();
            } else {
                return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isInSame(ComponentScope scope, Component other) {
            requireNonNull(other, "other is null");
            return component.isInSame(scope, ((ComponentAdaptor) other).component);
        }

        /** {@inheritDoc} */
        @Override
        public Application application() {
            return component.application.adaptor();
        }

        /** {@inheritDoc} */
        @Override
        public Component root() {
            ComponentSetup c = component;
            while (c.parent != null) {
                c = c.parent;
            }
            return c == component ? this : c.adaptor();
        }
    }
}
