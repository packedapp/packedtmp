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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.stream.Stream;

import app.packed.bindings.Key;
import app.packed.container.BaseAssembly;
import app.packed.operation.OperationMirror;
import internal.app.packed.service.ProvidedService;

/**
 * A mirror that represents a service provision operation.
 * 
 * @see Provide
 * @see BaseAssembly#provide(Class)
 * @see ProvideableBeanConfiguration#provide()
 */
// Maaske er export med i path'en istedet for et saelvstaendig mirror
// Her taenker jeg fx alle de wirelets der mapper fra og til...
public class ProvidedServiceMirror extends OperationMirror {

    /** The service that is provided. */
    final ProvidedService service;

    public ProvidedServiceMirror(@SuppressWarnings("exports") ProvidedService ps) {
        this.service = requireNonNull(ps);
    }

    /** {@return the key of the service.} */
    public Key<?> key() {
        return service.entry.key;
    }

    /**
     * Returns a stream of all the places where the provided value is directly used.
     * 
     * @return
     */
    public Stream<ServiceBindingMirror> useSites() {
        ArrayList<ServiceBindingMirror> l = new ArrayList<>();
        for (var b = service.entry.bindings; b != null; b = b.nextFriend) {
            l.add((ServiceBindingMirror) b.mirror());
        }
        return l.stream();
    }
}

// provide(Doo.class) -> BeanOperation.element = BeanClass  (Kunne ogsaa vaere constructoren???)
// provide(Doo.class) -> BeanOperation.element = BeanClass
