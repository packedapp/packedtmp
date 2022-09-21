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
package app.packed.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import app.packed.operation.OperationMirror;

/**
 *
 */
// Do we ever return empty trees? Det tror jeg ikke.

// Fx en Lifetime er jo altid et component tree...

// TreeView

//// TreeView<ComponentMirror>
//// TreeView<ContainerMirror>
//// TreeView<T extends Extension<?>>

// Maaske er den bedre i .container?


// Bliver den brugt andre steder end fra Lifetime????
// Hvis ikke skal vi vel bare embedde den...


// Rename ComponentMirror -> LifetimeElementMirror?
public interface ComponentMirrorTree  {

    /** {@return {@code true} if this set contains no mirrors.} */
    boolean isEmpty();

    /** {@return the number of components in this set.} */
    int size();

    Stream<ComponentMirror> stream();
    
    /**
     * @return
     * 
     * @see ComponentMirror#operations()
     */
    default List<OperationMirror> operations() {
        ArrayList<OperationMirror> operations = new ArrayList<>();
//        for (ComponentMirror cm : this) {
//            if (cm instanceof BeanMirror m) {
//                operations.addAll(m.operations());
//            }
//        }
        return Collections.unmodifiableList(operations);
    }

    /** {@return the root of the tree.} */
    ComponentMirror root(); // Optional<CM> if we have empty trees. Which we probably have with filtering
}
//Her er taenkt paa en Path fra From to To
//Minder maaske lidt for meget om ComponnetMirror.Relation
//interface ComponentMirrorPath extends Iterable<ComponentMirror> {
//
// ComponentMirror from();
// 
// ComponentMirror to();
//}
