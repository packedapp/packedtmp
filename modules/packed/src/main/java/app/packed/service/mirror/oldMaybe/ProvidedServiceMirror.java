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

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.binding.Key;
import app.packed.namespace.NamespaceOperationMirror;
import app.packed.operation.OperationHandle;
import app.packed.service.ServiceNamespaceMirror;
import app.packed.service.mirror.NamespaceServiceBindingMirror;
import internal.app.packed.service.ServiceProviderSetup;

/**
 * A mirror that represents a service provision operation.
 *
 * @see Provide
 * @see BaseAssembly#provide(Class)
 * @see ProvideableBeanConfiguration#provide()
 */
// ServiceProvisionSiteMirror
// Maaske er export med i path'en istedet for et saelvstaendig mirror
// Her taenker jeg fx alle de wirelets der mapper fra og til...
public class ProvidedServiceMirror extends NamespaceOperationMirror {

    /** The service that is provided. */
    final ServiceProviderSetup service;

    public ProvidedServiceMirror(OperationHandle<?> handle, ServiceProviderSetup ps) {
        super(handle);
        this.service = requireNonNull(ps);
    }

    /** {@return the key of the service.} */
    public Key<?> key() {
        return service.key();
    }

    /**
     * Returns a stream of all the places where the provided value is directly used.
     *
     * @return
     */

    // Det her er den eneste interessant ting klasser bruges til
    // Kan vi have en mere general API saa kan vi dropper denne klasse
    // Vi vil jo gerne vide hvor bliver den her bean, service, ect brugt henne...
    public Stream<NamespaceServiceBindingMirror> useSites() {
        throw new UnsupportedOperationException();
//        return service.entry.useSiteMirrors();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceNamespaceMirror namespace() {
        throw new UnsupportedOperationException();
    }
}

// provide(Doo.class) -> BeanOperation.element = BeanClass  (Kunne ogsaa vaere constructoren???)
// provide(Doo.class) -> BeanOperation.element = BeanClass
