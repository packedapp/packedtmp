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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import app.packed.util.TreeView;
import internal.app.packed.ValueBased;
import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;
import sandbox.lifetime.ContainerLifetimeCarrierMirror;

/**
 * A composite lifetime is a lifetime where other lifetimes may be available, or multiple beans may be present.
 */
@ValueBased
public final class CompositeLifetimeMirror extends LifetimeMirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(LifetimeSetup)}.
     */
    private final ContainerLifetimeSetup lifetime;

    /** Creates a new lifetime mirror. */
    CompositeLifetimeMirror(ContainerLifetimeSetup lifetime) {
        this.lifetime = requireNonNull(lifetime);
    }

    /**
     * Returns all the beans that contained with this lifetime in dependency order.
     * <p>
     * This include static, container, lazy. (IDK how they are ordered, static are proberly before anyone else)
     *
     * @return all the beans that contained with this lifetime
     */
    public Stream<BeanMirror> beans() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return Objects.hash(lifetime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompositeLifetimeMirror other = (CompositeLifetimeMirror) obj;
        return Objects.equals(lifetime, other.lifetime);
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
        return lifetime;
    }

    /** {@return any parent lifetime this lifetime is contained within.} */
    @Override
    public Optional<CompositeLifetimeMirror> parent() {
        return Optional.ofNullable(lifetime.treeParent).map(e -> e.mirror());
    }

    /**
     * @return
     *
     * @see ContainerMirror#beansInSameLifetime()
     */
    public TreeView<ContainerMirror> tree() {
        throw new UnsupportedOperationException();
    }

}

///// replaced by Container.beanInSameLifetime
///**
//* A map of all the containers contained in this lifetime as keys. And a collection for each container for their
//* respectively beans that are included in the lifetime.
//*
//* @return
//*/
//// Vil egentlig jo bare godt have en Tree her?
//// Men et tree er jo Tree<T> og vi har brug Tree<T, C>
//// Det er et tree hvor leafs and internals node are 2 different types
//// ComponentTree a tree where leads!= internal nodes
//// Maaske har vi bare nogle tools til at visualisere trees
//// Ordered in one way? Depth first?
//public /* Ordered */ Map<ContainerMirror, /* Ordered */ Collection<BeanMirror>> elements() {
//  return new AbstractMap<ContainerMirror, Collection<BeanMirror>>() {
//
//      @Override
//      public Set<Entry<ContainerMirror, Collection<BeanMirror>>> entrySet() {
//
//          throw new UnsupportedOperationException();
//      }
//  };
//  // beanKind.isInContainerLifetime()
//}

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
