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
package app.packed.service.mirror.oldMaybe;

import java.util.Map;

import app.packed.binding.Key;
import app.packed.service.ServiceContract;

/**
 *
 */
//
interface ServiceContainerMirror {
    /**
     * {@return a service contract for the container}
     * <p>
     * If the configuration of the container has not been completed. This method return a contract on a best effort basis.
     */
    ServiceContract serviceContract();

    // Detaljeret info, ogsaa med dependency graph som kan extractes...
    // Hvad skal vi returnere???

    // ServiceRegister, hvor hver service har specielle attributer??
    // Et Map af <Key, ServiceMirror> (Helt sikkert service mirror)
    // MapView<Key<?>, ServiceMirror>

    // or contract.keys()

    // Map<K, V> resolved
    // Map<K, V> unresolvedOptional?();

    /** { @return a map of all the services that are exported by the container.} */
    Map<Key<?>, ExportedServiceMirror> serviceExports();

    /** { @return a map of all the services that are provided internally in the container.} */
    Map<Key<?>, ProvidedServiceMirror> serviceProviders();
}
