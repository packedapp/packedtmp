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

import app.packed.operation.KeyBasedBindingMirror;
import internal.app.packed.service.ServiceBindingSetup;

/**
 * A binding of a service.
 */
// findAll(SBM.class).filterOn(key.equals(String.class)).toList();

// extends OnKeyBindingMirror?
public class ServiceBindingMirror extends KeyBasedBindingMirror {

    /** The service binding */
    private final ServiceBindingSetup binding;

   public ServiceBindingMirror(@SuppressWarnings("exports") ServiceBindingSetup binding) {
        this.binding = requireNonNull(binding);
    }

    /** {@return a mirror of the service extension.} */
    /// Hvad goer vi med extension beans?? De har jo saadan set en anden realm.
    public ServiceExtensionMirror extension() {
        return operation().bean().container().use(ServiceExtensionMirror.class);
    }

    /** {@return whether or not the service is required.} */
    public boolean isRequired() {
        return binding.required;
    }

    public boolean isResolved() {
        return binding.isResolved();
    }

    public boolean isSatisfiable() {
        return !isRequired() || isResolved();
    }

    /** {@return the binding key.} */
    public Key<?> key() {
        return binding.entry.key;
    }

    public Optional<ProvidedServiceMirror> providedService() {
        throw new UnsupportedOperationException();
    }
}
