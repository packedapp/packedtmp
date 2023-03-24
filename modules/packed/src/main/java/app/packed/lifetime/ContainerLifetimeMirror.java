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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import app.packed.util.Nullable;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import sandbox.lifetime.ContainerLifetimeCarrierMirror;

/**
 * This mirror represents the lifetime of a container.
 */
public final class ContainerLifetimeMirror extends LifetimeMirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(LifetimeSetup)}.
     */
    @Nullable
    private ContainerLifetimeSetup lifetime;

    /** Creates a new lifetime mirror. */
    public ContainerLifetimeMirror() {}

    /**
     * Returns all the beans that contained with this lifetime in dependency order.
     * <p>
     * This include static, container, lazy.
     *
     * @return all the beans that contained with this lifetime
     */
    // Containers can be optained from elements
    public /* Ordered */ Collection<BeanMirror> beans() {
        throw new UnsupportedOperationException();
    }

    /**
     * If this is container or application lifetime and it has a container lifetime wrapper bean. Returns the bean,
     * otherwise empty.
     * <p>
     * A wrapper and {@link #managedBy()} is always two different beans.
     *
     * @return
     */
    @SuppressWarnings("exports")
    public Optional<ContainerLifetimeCarrierMirror> carrier() {
        return Optional.empty();
    }

    // Vil egentlig jo bare godt have en Tree her?
    // Men et tree er jo Tree<T> og vi har brug Tree<T, C>
    // Det er et tree hvor leafs and internals node are 2 different types
    // ComponentTree a tree where leads!= internal nodes
    // Maaske har vi bare nogle tools til at visualisere trees

    /** {@return the container that is the root of the lifetime.} */
    public ContainerMirror container() {
        return lifetime().container.mirror();
    }

    /**
     * A map of all the containers contained in this lifetime as keys. And a collection for each container for their
     * respectively beans that are included in the lifetime.
     *
     * @return
     */
    // Ordered in one way? Depth first?
    public /* Ordered */ Map<ContainerMirror, /* Ordered */ Collection<BeanMirror>> elements() {
        // beanKind.isInContainerLifetime()
        // alternative BiStream
        throw new UnsupportedOperationException();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     *
     * @param owner
     *            the internal configuration of the extension to mirror
     */
    void initialize(ContainerLifetimeSetup lifetime) {
        if (this.lifetime != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.lifetime = lifetime;
    }

    // What about app-on-app?
    public boolean isApplicationRoot() {
        return lifetime().parent() == null;
    }

    /**
     * {@return the internal configuration of operation.}
     *
     * @throws IllegalStateException
     *             if {@link #initialize(ApplicationSetup)} has not been called.
     */
    @Override
    ContainerLifetimeSetup lifetime() {
        ContainerLifetimeSetup a = lifetime;
        if (a == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return a;
    }

    /** {@return any parent lifetime this lifetime is contained within.} */
    @Override
    public Optional<ContainerLifetimeMirror> parent() {
        return Optional.ofNullable(lifetime().treeParent).map(e -> e.mirror());
    }

}
///** {@return the root of the tree.} */
// LifetimeOriginMirror root(); // Optional<CM> if we have empty trees. Which we probably have with filtering

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
