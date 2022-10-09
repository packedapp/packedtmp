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
package internal.app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ComponentMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.util.LookupUtil;

/** Abstract build-time setup of a component. */
public abstract sealed class ComponentSetup permits ContainerSetup, BeanSetup {

    /** A handle that can access BeanMirror#bean. */
    private static final VarHandle BEAN_MIRROR_BEAN_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanMirror.class, "bean",
            BeanSetup.class);

    /** A handle that can access ContainerMirror#container. */
    private static final VarHandle CONTAINER_MIRROR_CONTAINER_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ContainerMirror.class,
            "container", ContainerSetup.class);

    /** The application this component is a part of. */
    public final ApplicationSetup application;

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

    /** The realm used to install this component. */
    public final RealmSetup realm;

    public final ArrayList<Runnable> wiringActions = new ArrayList<>(1);

    /**
     * Create a new component. This constructor is only invoked from subclasses of this class
     * 
     * @param application
     *            the application the component is a part of
     * @param realm
     *            the realm this component is part of
     * @param parent
     *            any parent component this component might have
     */
    protected ComponentSetup(ApplicationSetup application, RealmSetup realm, @Nullable ContainerSetup parent) {
        this.application = requireNonNull(application);
        this.realm = requireNonNull(realm);

        this.parent = parent;
        if (parent == null) {
            this.lifetime = new LifetimeSetup((ContainerSetup) this, null);
        } else {
            this.onWireAction = parent.onWireAction;
            this.lifetime = parent.lifetime;
        }
        realm.wireNew(this);
    }

    public final void checkIsCurrent() {
        if (!isCurrent()) {
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

    public final boolean isCurrent() {
        return realm.isCurrent(this);
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
        checkIsCurrent();

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

    public final void onWired() {
        for (Runnable action : wiringActions) {
            action.run();
        }
        if (onWireAction != null) {
            onWireAction.accept(mirror());
        }
    }

    /** {@return the path of this component} */
    public abstract NamespacePath path();

    public abstract Stream<ComponentSetup> stream();

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

    public static ComponentSetup crackMirror(ComponentMirror mirror) {
        if (mirror instanceof BeanMirror m) {
            return (BeanSetup) BEAN_MIRROR_BEAN_HANDLE.get(m);
        } else {
            return (ContainerSetup) CONTAINER_MIRROR_CONTAINER_HANDLE.get((ContainerMirror) mirror);
        }
    }
}
