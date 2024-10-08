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

import app.packed.extension.BaseExtension;

/**
 *
 */
public final class ServiceNamespaceBridge extends NamespaceBridge<BaseExtension> {

    // Her er Service Namespace nok meget specielt. Maaske sammen med EventBusNamespace
    // At vi tager ting ind...
    public ServiceNamespaceBridge incoming(Object transformer) {
        throw new UnsupportedOperationException();
    }

    // Is this supported for app-on-app?
    public ServiceNamespaceBridge outgoing(Object transformer) {
        throw new UnsupportedOperationException();
    }

    public ServiceNamespaceBridge bothWays(Object transformerIn, Object transformerOut) {
        throw new UnsupportedOperationException();
    }
}
