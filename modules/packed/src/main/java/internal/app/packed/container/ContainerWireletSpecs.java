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
package internal.app.packed.container;

import java.util.function.Consumer;

import app.packed.service.bridge.ServiceNamespaceBridge;

/**
 *
 */
// Maybe BaseExtensionSupport??
// Vi har det jo her fordi det er svaert at tilgaa BaseExtension fra interne pakker
public class ContainerWireletSpecs {

    public String name;

    public boolean newServiceNamespace;

    // I think we need to run this, even if we don't use a service namespace?
    public Consumer<? super ServiceNamespaceBridge> serviceBridges;
}
