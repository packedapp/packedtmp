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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirror.Relation;
import app.packed.component.ComponentScope;
import app.packed.component.UserOrExtension;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import packed.internal.application.ApplicationSetup;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ContainerRealmSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionApplicationSetup;
import packed.internal.container.RealmSetup;
import packed.internal.lifetime.LifetimeSetup;

/** Abstract build-time setup of a component. */
public abstract sealed class ComponentSetup permits ContainerSetup,BeanSetup {

    /** The application this component is a part of. */
    public final ApplicationSetup application;

    /** The depth of the component in the application tree. */
    public final int depth;

    /** The lifetime the component is a part of. */
    public final LifetimeSetup lifetime;

    /** The name of this component. */
    @Nullable
    public String name;

    /**
     * An action that, if present, must be called whenever the component has been completely wired.
     * <p>
     * This field is not final as it may be updated later via wirelets.
     */
    @Nullable
    public Consumer<? super ComponentMirror> onWireAction;

    /** The container this component belongs to, or null for a root container. */
    @Nullable
    public final ContainerSetup parent;

    /** The realm this component is a part of. */
    public final RealmSetup realm;

    public final ContainerRealmSetup assembly;

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
    protected ComponentSetup(ApplicationSetup application, RealmSetup realm, LifetimeSetup lifetime, @Nullable ContainerSetup parent) {
        this.application = requireNonNull(application);
        this.realm = requireNonNull(realm);
        this.lifetime = requireNonNull(lifetime);

        if (realm instanceof ContainerRealmSetup s) {
            this.assembly = s;
        } else {
            this.assembly = parent.assembly;
        }

        this.parent = parent;
        if (parent == null) {
            this.depth = 0;
        } else {
            this.depth = parent.depth + 1;
            this.onWireAction = parent.onWireAction;
        }
    }

    public final void checkIsActive() {
        if (realm.currentComponent() != this) {
            String errorMsg;
            // if (realm.container == this) {
            errorMsg = "This operation must be called as the first thing in Assembly#build()";
            // } else {
            // errorMsg = "This operation must be called immediately after the component has been wired";
            // }
            // is it just named(), in that case we should say it explicityly instead of just saying "this operation"
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
            if (c.size() == 0) {
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
        // Need to check namespace as well fx for
        case CONTAINER -> parent == other.parent; // does not work for root
        case APPLICATION -> application == other.application;
        case COMPONENT -> this == other;
        case NAMESPACE -> application /* .build.namespace */ == other.application /* .build.namespace */;
        };
    }

    /** {@inheritDoc} */
    public abstract ComponentMirror mirror();

    /** {@inheritDoc} */
    public final void named(String newName) {
        // We start by validating the new name of the component
        checkComponentName(newName);

        // Check that this component is still active and the name can be set
        checkIsActive();

        String currentName = this.name;

        if (newName.equals(currentName)) {
            return;
        }

        // If the name of the component (container) has been set using a wirelet.
        // Any attempt to override will be ignored
        if (this instanceof ContainerSetup cs && cs.isNameInitializedFromWirelet) {
            return;
        }

        // Unless we are the root container. We need to insert this component in the parent container
        if (parent != null) {
            if (parent.children.putIfAbsent(newName, this) != null) {
                throw new IllegalArgumentException("A component with the specified name '" + newName + "' already exists");
            }
            parent.children.remove(currentName);
        }
        this.name = newName;
    }

    public void onWired() {
        if (onWireAction != null) {
            onWireAction.accept(mirror());
        }
    }

    /** {@return the path of this component} */
    public final NamespacePath path() {
        return PackedNamespacePath.of(this);
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
    public abstract class AbstractBuildTimeComponentMirror {

        /** {@inheritDoc} */
        public final ApplicationMirror application() {
            return application.mirror();
        }

        public final AssemblyMirror assembly() {
            throw new UnsupportedOperationException();
        }
        
        protected abstract Collection<ComponentMirror> children();

        /** {@inheritDoc} */
        public final int depth() {
            return depth;
        }

        /** {@inheritDoc} */
        public final boolean isInSame(ComponentScope scope, ComponentMirror other) {
            requireNonNull(other, "other is null");
            return ComponentSetup.this.isInSame(scope, ((AbstractBuildTimeComponentMirror) other).outer());
        }

        /** {@inheritDoc} */
        public final String name() {
            return name;
        }

        private ComponentSetup outer() {
            return ComponentSetup.this;
        }

        /** {@inheritDoc} */
        public final Optional<ContainerMirror> parent() {
            ContainerSetup parent = ComponentSetup.this.parent;
            return parent == null ? Optional.empty() : Optional.of(parent.mirror());
        }

        /** {@inheritDoc} */
        public final NamespacePath path() {
            return ComponentSetup.this.path();
        }

        /** {@inheritDoc} */
        public final UserOrExtension owner() {
            if (realm instanceof ExtensionApplicationSetup s) {
                return UserOrExtension.extension(s.extensionModel.type());
            }
            return UserOrExtension.user();
        }

        /** {@inheritDoc} */
        public final Relation relationTo(ComponentMirror other) {
            requireNonNull(other, "other is null");
            return ComponentSetupRelation.of(ComponentSetup.this, ((AbstractBuildTimeComponentMirror) other).outer());
        }

        /** {@inheritDoc} */
        public final ComponentMirror resolve(CharSequence path) {
            LinkedHashMap<String, ComponentSetup> map = ((ContainerSetup) ComponentSetup.this).children;
            if (map != null) {
                ComponentSetup cs = map.get(path.toString());
                if (cs != null) {
                    return cs.mirror();
                }
            }
            throw new UnsupportedOperationException();
        }

//
//        /** {@inheritDoc} */
//        public final ContainerMirror root() {
//            ContainerSetup c = parent;
//            if (c == null) {
//                return (ContainerMirror) this;
//            } else {
//                while (c.parent != null) {
//                    c = c.parent;
//                }
//                return c.mirror();
//            }
//        }
//
        /** {@inheritDoc} */
        public final Stream<ComponentMirror> components() {
            return stream0(ComponentSetup.this, true);
        }

        private Stream<ComponentMirror> stream0(ComponentSetup origin, boolean isRoot) {
            // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Collection<AbstractBuildTimeComponentMirror> c = (Collection) children();
            if (c != null && !c.isEmpty()) {
                Stream<ComponentMirror> s = c.stream().flatMap(co -> co.stream0(origin, false));
                return /* isRoot && option.excludeOrigin() ? s : */ Stream.concat(Stream.of(thisMirror()), s);
                // return Stream.empty();
            } else {
                return /* isRoot && option.excludeOrigin() ? Stream.empty() : */ Stream.of(thisMirror());
            }
        }

        private ComponentMirror thisMirror() {
            return (ComponentMirror) this;
        }
    }
}
