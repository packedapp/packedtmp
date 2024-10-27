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
package app.packed.service.bridge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.service.ServiceContract;

/**
 * A service bridge between two service namespaces.
 */
// Tror faktisk problem er her.
// At det er 2 forskellige consumers vi skal bruge.
// Incoming skal koeres foerst. Forend vi kan koere outgoing.
public interface ServiceNamespaceBridge {

    ServiceBridge incoming();

    ServiceBridge outgoing();

    default Set<? extends Key<?>> unresolved() {
        HashSet<Key<?>> s = new HashSet<>(childContract().requires());
        s.removeAll(availableKeys());
        return Collections.unmodifiableSet(s);
    }

    Set<? extends Key<?>> availableKeys();

    // The contract of the child container
    ServiceContract childContract();
}
