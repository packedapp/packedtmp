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

import java.util.Optional;

import app.packed.bean.BeanMirror;
import app.packed.operation.BindingMirror;
import app.packed.util.Key;
import internal.app.packed.service.ServiceBindingSetup;

/**
 * A binding of a service.
 */
// findAll(SBM.class).filterOn(key.equals(String.class)).toList();

// Hvor faar vi den fra successfuld
//// En Bean (constant)
//// En Lifetime bean
//// En prototypeBean
//// En @Provide method

// unsuccessfull
//// Missing
//// missing but Optional
//// missing but default

// Maaske er ServiceBinding altid en service, og det andet er en manuel binding
public class ServiceBindingMirror extends BindingMirror {

    /** The service binding */
    private final ServiceBindingSetup binding;

    public ServiceBindingMirror(@SuppressWarnings("exports") ServiceBindingSetup binding) {
        this.binding = requireNonNull(binding);
    }

    /** {@return the domain this service is provided from.} */
    public ServiceNamespaceMirror domain() {
        throw new UnsupportedOperationException();
    }

    /** {@return whether or not the service is required.} */
    public boolean isRequired() {
        return binding.isRequired;
    }

    public boolean isResolved() {
        return binding.isResolved();
    }

    public boolean isSatisfiable() {
        return !isRequired() || isResolved();
    }

    /** {@inheritDoc} */
    public Key<?> key() {
        return binding.entry.key;
    }

    public Optional<BeanMirror> providedBy() {
        throw new UnsupportedOperationException();
    }

    // non null if resolvedx
    // Der er noget med en sti til servicen.
    public Optional<ServiceMirror> providingService() {
        throw new UnsupportedOperationException();
    }
}
