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
package app.packed.namespace.bridge;

import app.packed.extension.Extension;

/**
 * A namespace bridge server as a communication link between 2 or more namespaces of the same kind. Typically, the
 * namespaces are in the same container or containers in a parent/child relationship.
 */
// Promulgation
// Haandtere child<->parent promulgation

// What about sideways Namespace->Othernamespace Either own or Extension in same container
// Was promulgator
// Hmm interface?
// IDK Not sure we need one.
public abstract class NamespaceBridge<E extends Extension<E>> {

    // Maybe they do extend wirelet. Maybe they can only be specified via wirelets
    // Or inter container
    // Would be nice with something immutable if it is a wirelet
//    public final ExtensionWirelet<E> toWirelet() {
//        throw new UnsupportedOperationException();
//    }
}

// Application methods
//// Install Method, Wirelets
// Internally in a container via some TBD methods
