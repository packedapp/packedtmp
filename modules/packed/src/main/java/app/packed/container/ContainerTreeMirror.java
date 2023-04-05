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

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import app.packed.context.ContextScopeMirror;
import internal.app.packed.container.Mirror;

/**
 * Represents one or more containers ordered in a tree with a single node as the root.
 * <p>
 * Unless otherwise specified the tree is ordered accordingly to the installation order of each container.
 */
// Vi skal have den fordi namespace simpelthen bliver noedt til at definere den
// Vi har en main database der bruges i P og saa bruger vi den i C1, C2 bruger den under alias "NotMain", og definere sin egen main.
// C3 definere kun sig egen main

// Hvis vi siger at et domain er hele appen. Hvad goere vi i C3. Er den tilgaengelig under et "fake" navn???
//

// Alternative ContainerMirror.ofTree
public non-sealed interface ContainerTreeMirror extends Mirror, ContextScopeMirror {

    void forEach(BiConsumer<Integer, ContainerMirror> action);

    /** {@return the root container in the tree.} */
    ContainerMirror root();

    /** {@return the number of containers in the tree. */
    int size();

    /** {@return a stream with all containers in the tree, depth first.} */
    Stream<ContainerMirror> stream();

    interface Node {
        int depth();
        ContainerMirror container();
    }
}
