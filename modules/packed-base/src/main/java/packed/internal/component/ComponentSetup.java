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
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentScope;
import app.packed.component.Operator;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import packed.internal.application.ApplicationSetup;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.component.bean.BeanSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.CollectionUtil;

/** Abstract build-time setup of a component. */
public abstract sealed class ComponentSetup permits ContainerSetup,BeanSetup {

    /** The application this component is a part of. */
    public final ApplicationSetup application;

    /** Children of this node (lazily initialized) in insertion order. */
    @Nullable
    LinkedHashMap<String, ComponentSetup> children;

    /** The container this component is a part of. A container is a part of it self. */
    public final ContainerSetup container;

    /** The depth of the component in the tree. */
    protected final int depth;

    /** The lifetime of this component. */
    public final LifetimeSetup lifetime;

    /** The name of this component. */
    public String name;

    /** An action that, if present, must be called whenever the component has been completely wired. */
    @Nullable
    public Consumer<? super ComponentMirror> onWire;

    /** The parent of this component, or null for a root component. */
    @Nullable
    protected final ComponentSetup parent;

    /** The realm this component is a part of. */
    public final RealmSetup realm;

    /**
     * Create a new component. This constructor is only invoked from subclasses of this class
     * 
     * @param application
     *            the application the component is a part of
     * @param realm
     *            the realm this component is part of
     * @param lifetime
     *            the lifetime this component is part of
     * @param parent
     *            any parent component this component might have
     */
    protected ComponentSetup(ApplicationSetup application, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent) {
        this.parent = parent;
        if (parent == null) {
            this.depth = 0;
        } else {
            this.depth = parent.depth + 1;
            this.onWire = parent.onWire;
        }

        this.realm = requireNonNull(realm);
        this.lifetime = requireNonNull(lifetime);
        this.application = requireNonNull(application);
        this.container = this instanceof ContainerSetup container ? container : parent.container;
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

    /** {@inheritDoc} */
    public abstract ComponentMirror mirror();

    /** {@inheritDoc} */
    public final void named(String name) {
        checkComponentName(name); // Check if the name is valid
        checkIsWiring();

        // If a name has been set using a wirelet it cannot be overridden
        if (this instanceof ContainerSetup cs && cs.nameInitializedWithWirelet) {
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
    public non-sealed abstract class AbstractBuildTimeComponentMirror implements ComponentMirror {

        /** {@inheritDoc} */
        @Override
        public final ApplicationMirror application() {
            return application.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public final Operator registrant() {
            Class<? extends Extension> extensionType = realm.extensionType;
            return extensionType == null ? Operator.user() : Operator.extension(extensionType);
        }

        /** {@inheritDoc} */
        @Override
        public final Collection<ComponentMirror> children() {
            LinkedHashMap<String, ComponentSetup> m = children;
            return m == null ? List.of() : CollectionUtil.unmodifiableView(m.values(), c -> c.mirror());
        }

        @Override
        public Stream<ComponentMirror> components() {
            return stream();
        }

        /** {@inheritDoc} */
        @Override
        public final ContainerMirror container() {
            return container.mirror();
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
