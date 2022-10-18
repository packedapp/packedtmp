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

import app.packed.base.Key;
import app.packed.operation.OperationMirror;
import internal.app.packed.operation.newInject.ProvidedService;

/**
 *
 */
// ServiceProvisionMirror

// permits InternalOp
public class ServiceProvisionMirror extends OperationMirror {

    final ProvidedService ps;

    ServiceProvisionMirror(ProvidedService ps) {
        this.ps = requireNonNull(ps);
    }

    /** {@return the key of the service.} */
    public Key<?> key() {
        return ps.entry.key;
    }

    // Local bindings?
    // Collection<ServiceBindingMirror> bindings();
}

// provide(Doo.class) -> BeanOperation.element = BeanClass  (Kunne ogsaa vaere constructoren???)
// provide(Doo.class) -> BeanOperation.element = BeanClass