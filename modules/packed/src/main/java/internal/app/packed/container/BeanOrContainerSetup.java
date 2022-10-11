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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifetime.LifetimeSetup;

/** Abstract build-time setup of a component. */
public abstract sealed class BeanOrContainerSetup permits ContainerSetup, BeanSetup {

    /** The name of this component. */
    @Nullable
    public String name;

    public abstract RealmSetup realm();
    
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

    public final boolean isCurrent() {
        return realm().isCurrent(this);
    }

    public abstract LifetimeSetup lifetime();

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
        if (parent() != null) {
            if (parent().children.putIfAbsent(newName, this) != null) {
                throw new IllegalArgumentException("A component with the specified name '" + newName + "' already exists");
            }
            parent().children.remove(currentName);
        }
        this.name = newName;
    }

    @Nullable
    protected abstract ContainerSetup parent();

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
}

//
///** A handle that can access BeanMirror#bean. */
//private static final VarHandle BEAN_MIRROR_BEAN_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanMirror.class, "bean",
//        BeanSetup.class);
//
///** A handle that can access ContainerMirror#container. */
//private static final VarHandle CONTAINER_MIRROR_CONTAINER_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ContainerMirror.class,
//        "container", ContainerSetup.class);
//
///**
// * Tests that this component is in the same specified scope as another component.
// * 
// * @param scope
// *            the scope to test
// * @param other
// *            the other component to test
// * @return true if in the same scope, otherwise false
// */
//public final boolean isInSame(ComponentScope scope, ComponentSetup other) {
//    requireNonNull(scope, "scope is null");
//    requireNonNull(other, "other is null");
//    return switch (scope) {
//    // Need to check namespace as well fx for
//    case CONTAINER -> throw new UnsupportedOperationException(); // parent == other.parent; // does not work for root
//    case APPLICATION -> throw new UnsupportedOperationException(); // application == other.application;
//    case COMPONENT -> this == other;
//    case NAMESPACE -> throw new UnsupportedOperationException(); // application /* .build.namespace */ == other.application /* .build.namespace */;
//    };
//}
//public static ComponentSetup crackMirror(ComponentMirror mirror) {
//    if (mirror instanceof BeanMirror m) {
//        return (BeanSetup) BEAN_MIRROR_BEAN_HANDLE.get(m);
//    } else {
//        return (ContainerSetup) CONTAINER_MIRROR_CONTAINER_HANDLE.get((ContainerMirror) mirror);
//    }
//}