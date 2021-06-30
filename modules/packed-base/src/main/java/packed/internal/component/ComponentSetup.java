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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.attribute.Attribute;
import app.packed.attribute.AttributeMap;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentScope;
import app.packed.component.Wirelet;
import app.packed.container.ContainerAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.BuildSetup;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedContainerDriver;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.CollectionUtil;
import packed.internal.util.ThrowableUtil;

/** Abstract build-time setup of a component. */
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

    /** The lifetime of the component. */
    public final LifetimeSetup lifetime;

    /** The name of this component. */
    protected String name;

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    boolean nameInitializedWithWirelet;

    /** An action that, if present, must be called whenever the component has been completely wired. */
    @Nullable
    protected Consumer<? super ComponentMirror> onWire;

    /** The parent of this component, or null for a root component. */
    @Nullable
    protected final ComponentSetup parent;

    /** The realm this component is a part of. */
    public final RealmSetup realm;

    /** Wirelets that was specified when creating the component. */
    // Alternativ er den ikke final.. men bliver nullable ud eftersom der ikke er flere wirelets
    @Nullable
    public final WireletWrapper wirelets;

    /**
     * Create a new component. This constructor is always invoked from one of subclasses of this class
     * 
     * @param build
     *            the build this component is a part of
     * @param realm
     *            the realm this component is part of
     * @param lifetime
     *            the lifetime this component is part of
     * @param driver
     *            the component driver for this component
     * @param parent
     *            any parent component this component might have
     * @param wirelets
     *            optional (unprocessed) wirelets specified by the user
     */
    protected ComponentSetup(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, PackedComponentDriver<?> driver, @Nullable ComponentSetup parent,
            Wirelet[] wirelets) {
        this.parent = parent;
        if (parent == null) {
            this.depth = 0;
        } else {
            this.depth = parent.depth + 1;
            this.onWire = parent.onWire;
        }

        this.build = requireNonNull(build);
        this.realm = requireNonNull(realm);
        this.lifetime = requireNonNull(lifetime);

        this.application = this instanceof ApplicationSetup application ? application : parent.application;
        this.container = this instanceof ContainerSetup container ? container : parent.container;

        // The rest of the constructor is just processing any wirelets that have been specified by
        // the user or extension when wiring the component. The wirelet's have not been null checked.
        // and may contained any number of CombinedWirelet instances.
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            this.wirelets = null;
        } else {
            // If it is the root
            Wirelet[] ws;
            if (parent == null) {
                if (driver.wirelet == null) {
                    ws = CombinedWirelet.flattenAll(wirelets);
                } else {
                    ws = CombinedWirelet.flatten2(driver.wirelet, Wirelet.combine(wirelets));
                }
            } else {
                ws = CombinedWirelet.flattenAll(wirelets);
            }

            this.wirelets = new WireletWrapper(ws);

            // May initialize the component's name, onWire, ect
            // Do we need to consume internal wirelets???
            // Maybe that is what they are...
            int unconsumed = 0;
            for (Wirelet w : ws) {
                if (w instanceof InternalWirelet bw) {
                    // Maaske er alle internal wirelets first passe
                    bw.onBuild(this);
                } else {
                    unconsumed++;
                }
            }
            if (unconsumed > 0) {
                this.wirelets.unconsumed = unconsumed;
            }

            if (nameInitializedWithWirelet && parent != null) {
                initializeNameWithPrefix(name);
                // addChild(child, name);
            }
        }
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

    public final void checkIsWiring() {
        if (realm.current() != this) {
            String errorMsg;
            if (realm.root == this) {
                errorMsg = "This operation must be called as the first thing in Assembly#build()";
            } else {
                errorMsg = "This operation must be called immediately after the component has been wired";
            }
            throw new IllegalStateException(errorMsg);
        }
    }

    //
    public final String getName() {
        // Dette kunne ogsaa wire componenten?
        return name;
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
                    n = name + counter++; // maybe store some kind of map<ComponentSetup, LongCounter> in BuildSetup.. for those that want to test adding 1
                                          // million of the same component type
                }
            }
        }
        this.name = n;
    }

    /**
     * Tests that this component is in the same specified scope as another component.
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
     * @param realm
     *            realm
     * @param wirelets
     *            optional wirelets
     * @return the component that was linked
     * @see ExtensionContext#link(Assembly, Wirelet...)
     */
    public final ContainerMirror link(ContainerAssembly<?> assembly, RealmSetup realm, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        PackedContainerDriver<?> driver = (PackedContainerDriver<?>) PackedComponentDriver.getDriver(assembly);

        // Create the new realm that should be used for linking
        RealmSetup newRealm = realm.link(driver, this, assembly, wirelets);

        // Create the component configuration that is needed by the assembly
        ContainerConfiguration configuration = driver.toConfiguration(newRealm.root);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the new realm again after the assembly has been successfully linked
        newRealm.close();

        return (ContainerMirror) newRealm.root.mirror();
    }

    /** {@inheritDoc} */
    public abstract ComponentMirror mirror();

    /** {@inheritDoc} */
    public final void named(String name) {
        checkComponentName(name); // Check if the name is valid
        checkIsWiring();

        // If a name has been set using a wirelet it cannot be overridden
        if (nameInitializedWithWirelet) {
            return;
        } else if (name.equals(this.name)) {
            return;
        }

        // maybe assume s==0

        if (parent != null) {
            parent.children.remove(this.name);
            if (parent.children.putIfAbsent(name, this) != null) {
                throw new IllegalArgumentException("A component with the specified name '" + name + "' already exists");
            }
        }
        this.name = name;
    }

    final void onWired() {
        if (onWire != null) {
            onWire.accept(mirror());
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

    public final <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, RealmSetup realm, Wirelet... wirelets) {
        PackedComponentDriver<C> drv = (PackedComponentDriver<C>) requireNonNull(driver, "driver is null");

        // Wire a new component
        ComponentSetup component = realm.wire(drv, this, wirelets);

        // Return a component configuration to the user
        return drv.toConfiguration(component);
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

    /** An mirror adaptor for {@link ComponentSetup}. */
    public abstract class AbstractBuildTimeComponentMirror implements ComponentMirror {

        /** {@inheritDoc} */
        @Override
        public final ApplicationMirror application() {
            return application.applicationMirror();
        }

        /** {@inheritDoc} */
        @Override
        public final Collection<ComponentMirror> children() {
            LinkedHashMap<String, ComponentSetup> m = children;
            return m == null ? List.of() : CollectionUtil.unmodifiableView(m.values(), c -> c.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public final ContainerMirror container() {
            return container.mirror();
        }

        @Override
        public Stream<ComponentMirror> components() {
            return stream();
        }

        /** {@inheritDoc} */
        @Override
        public final int depth() {
            return depth;
        }

        /** {@inheritDoc} */
        @Override
        public final boolean isInSame(ComponentScope scope, ComponentMirror other) {
            requireNonNull(other, "other is null");
            return ComponentSetup.this.isInSame(scope, ((AbstractBuildTimeComponentMirror) other).outer());
        }

        /** {@inheritDoc} */
        @Override
        public final String name() {
            return name;
        }

        private ComponentSetup outer() {
            return ComponentSetup.this;
        }

        /** {@inheritDoc} */
        @Override
        public final Optional<Class<? extends Extension>> memberOfExtension() {
            return Optional.ofNullable(realm.extensionType);
        }

        /** {@inheritDoc} */
        @Override
        public final Optional<ComponentMirror> parent() {
            ComponentSetup parent = ComponentSetup.this.parent;
            return parent == null ? Optional.empty() : Optional.of(parent.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public final NamespacePath path() {
            return ComponentSetup.this.path();
        }

        /** {@inheritDoc} */
        @Override
        public final Relation relationTo(ComponentMirror other) {
            requireNonNull(other, "other is null");
            return ComponentSetupRelation.of(ComponentSetup.this, ((AbstractBuildTimeComponentMirror) other).outer());
        }

        /** {@inheritDoc} */
        @Override
        public final ComponentMirror resolve(CharSequence path) {
            LinkedHashMap<String, ComponentSetup> map = children;
            if (map != null) {
                ComponentSetup cs = map.get(path.toString());
                if (cs != null) {
                    return cs.mirror();
                }
            }
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public final ComponentMirror root() {
            ComponentSetup c = ComponentSetup.this;
            while (c.parent != null) {
                c = c.parent;
            }
            return c == ComponentSetup.this ? this : c.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public final ComponentMirrorStream stream(ComponentMirrorStream.Option... options) {
            return new PackedComponentStream(stream0(ComponentSetup.this, true, PackedComponentStreamOption.of(options)));
        }

        private Stream<ComponentMirror> stream0(ComponentSetup origin, boolean isRoot, PackedComponentStreamOption option) {
            // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Collection<AbstractBuildTimeComponentMirror> c = (Collection) children();
            if (c != null && !c.isEmpty()) {
                if (option.processThisDeeper(origin, ComponentSetup.this)) {
                    Stream<ComponentMirror> s = c.stream().flatMap(co -> co.stream0(origin, false, option));
                    return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
                }
                return Stream.empty();
            } else {
                return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
            }
        }
    }
}
