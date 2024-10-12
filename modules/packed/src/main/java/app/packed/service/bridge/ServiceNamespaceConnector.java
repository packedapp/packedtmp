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

import java.util.function.Consumer;

import internal.app.packed.service.ServiceNamespaceHandle;

/**
 *
 */
// Connecte 2 namespaces
// Maa lazy nogle maps, der som udgangs har service handles
// Ved ikke om vi bliver noedt til at lave en fake bean with handles for map/replace?
// Vi kan vel godt
public class ServiceNamespaceConnector {
    ServiceNamespaceHandle from;
    ServiceNamespaceHandle to;

    public void apply(Consumer<? super ServiceNamespaceBridge> configurator) {

    }
}
