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
package app.packed.lifetime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.framework.Nullable;
import app.packed.operation.OperationMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;

/**
 * This mirror represents a container lifetime.
 */
public final class ContainerLifetimeMirror extends LifetimeMirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(LifetimeSetup)}.
     */
    @Nullable
    private ContainerLifetimeSetup lifetime;

    /**
     * Create a new operation mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public ContainerLifetimeMirror() {}

    /** {@return the container that is the root of the lifetime.} */
    public ContainerMirror container() {
        return lifetime().container.mirror();
    }

    public Map<ContainerMirror, Collection<BeanMirror>> elements() {
        // beanKind.isInContainerLifetime()
        // alternative BiStream
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof ContainerLifetimeMirror m && lifetime() == m.lifetime();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return lifetime().hashCode();
    }

    /**
     * If this is container or application lifetime and it has a container lifetime wrapper bean. Returns the bean,
     * otherwise empty.
     * <p>
     * A wrapper and {@link #managedBy()} is always two different beans.
     * 
     * @return
     */
    public Optional<BeanMirror> holderBean() { // Do we need a ContainerWrapperBeanMirror?
        return Optional.empty();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param owner
     *            the internal configuration of the extension to mirror
     */
    final void initialize(ContainerLifetimeSetup lifetime) {
        if (this.lifetime != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.lifetime = lifetime;
    }

    public boolean isRoot() {
        return lifetime().parent() == null;
    }

    /**
     * {@return the internal configuration of operation.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(ApplicationSetup)} has not been called.
     */
    private ContainerLifetimeSetup lifetime() {
        ContainerLifetimeSetup a = lifetime;
        if (a == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return a;
    }

    /**
     * @return
     * 
     */
    public List<OperationMirror> operations2() {
        ArrayList<OperationMirror> operations = new ArrayList<>();
//        for (ComponentMirror cm : this) {
//            if (cm instanceof BeanMirror m) {
//                operations.addAll(m.operations());
//            }
//        }
        return Collections.unmodifiableList(operations);
    }

    /** {@return any parent lifetime this lifetime is contained within.} */
    public Optional<ContainerLifetimeMirror> parent() {
        return Optional.ofNullable(lifetime().treeParent).map(e -> e.mirror());
    }

    /** {@return the root of the tree.} */
    // LifetimeOriginMirror root(); // Optional<CM> if we have empty trees. Which we probably have with filtering
}
//Do we ever return empty trees? Det tror jeg ikke.

//Fx en Lifetime er jo altid et component tree...

//TreeView

////TreeView<ComponentMirror>
////TreeView<ContainerMirror>
////TreeView<T extends Extension<?>>

//Maaske er den bedre i .container?

//Her er taenkt paa en Path fra From to To
//Minder maaske lidt for meget om ComponnetMirror.Relation
//interface ComponentMirrorPath extends Iterable<ComponentMirror> {
//
// ComponentMirror from();
// 
// ComponentMirror to();
//}
