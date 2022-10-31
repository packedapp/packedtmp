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
import app.packed.operation.OperationMirror;
import internal.app.packed.lifetime.ContainerLifetimeSetup;

/**
 * This mirror represents a container lifetime.
 */
public final class ContainerLifetimeMirror extends LifetimeMirror {

    /** {@return the container that is the root of the lifetime.} */
    public ContainerMirror container() {
        return setup().container.mirror();
    }

    public Map<ContainerMirror, Collection<BeanMirror>> elements() {
        // beanKind.isInContainerLifetime()
        // alternative BiStream
        throw new UnsupportedOperationException();
    }

    /**
     * If this is container or application lifetime and it has a container lifetime wrapper bean. Returns the bean,
     * otherwise empty.
     * <p>
     * A wrapper and {@link #managedByBean()} is always two different beans.
     * 
     * @return
     */
    public Optional<BeanMirror> holderBean() { // Do we need a ContainerWrapperBeanMirror?
        return Optional.empty();
    }

    public boolean isRoot() {
        return lifetime().parent == null;
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

    private ContainerLifetimeSetup setup() {
        return (ContainerLifetimeSetup) lifetime();
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
