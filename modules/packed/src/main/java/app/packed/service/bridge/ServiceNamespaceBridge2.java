/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionWirelet;
import app.packed.namespace.bridge.NamespaceBridge;
import app.packed.service.ServiceContract;

/**
 * A service bridge between two service namespaces.
 */
public abstract class ServiceNamespaceBridge2 implements NamespaceBridge<BaseExtension> {

    public abstract ServiceBridge incoming();

    public abstract ServiceBridge outgoing();

    public Set<? extends Key<?>> missing() {
        HashSet<Key<?>> s = new HashSet<>(childContract().requires());
        s.removeAll(availableKeys());
        return Collections.unmodifiableSet(s);
    }

    public abstract Set<? extends Key<?>> availableKeys();

    // The contract of the child container
    public abstract ServiceContract childContract();


    // Maybe it is also here we configure exactly how it comes in
    public static ExtensionWirelet<BaseExtension> wireletOf(Consumer<? super ServiceNamespaceBridge2> configurator) {
        throw new UnsupportedOperationException();
    }
//
//    public void renameImport(Key<?> from, Key<?> to) {}
//    public void renameExport(Key<?> from, Key<?> to) {}
//
//    // Her er Service Namespace nok meget specielt. Maaske sammen med EventBusNamespace
//    // At vi tager ting ind...
//    public ServiceNamespaceBridge incoming(Object transformer) {
//        throw new UnsupportedOperationException();
//    }
//
//    // Is this supported for app-on-app?
//    public ServiceNamespaceBridge outgoing(Object transformer) {
//        throw new UnsupportedOperationException();
//    }
//
//    public ServiceNamespaceBridge bothWays(Object transformerIn, Object transformerOut) {
//        throw new UnsupportedOperationException();
//    }
}
