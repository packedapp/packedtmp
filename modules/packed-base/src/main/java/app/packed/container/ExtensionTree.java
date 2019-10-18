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
package app.packed.container;

/**
 *
 */
// foreach, will take each node in top->down manner
public interface ExtensionTree<E extends Extension> extends Iterable<E> {

    /**
     * Returns the number of nodes in this tree.
     * 
     * @return the number of nodes in this tree
     */
    int size();

    int degree();

    // Host...
}

// Tree Operations

// get Root(s)
// is Fully connected
// size()

// Node operations
// boolean isRoot();
// Tree connectedTree();
// root
// parent
// children
// sieblings
// forEachChild
// int index.... from [0 to size-1] In order of usage????
