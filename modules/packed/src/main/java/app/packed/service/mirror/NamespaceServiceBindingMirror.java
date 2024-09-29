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
package app.packed.service.mirror;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.binding.Key;
import app.packed.binding.sandbox.BindingHandle;
import app.packed.service.ServiceNamespaceMirror;
import app.packed.service.mirror.oldMaybe.ServiceBindingMirror;
import internal.app.packed.service.ServiceBindingSetup;

/**
 * A binding of a service.
 * <p>
 * I virkeligheden eksistere der ikke noedvendig en service.
 * Men bindingen er blevet resolvet som en service
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
public class NamespaceServiceBindingMirror extends ServiceBindingMirror {

    /** The service binding */
    private final ServiceBindingSetup binding;

    public NamespaceServiceBindingMirror(@SuppressWarnings("exports") BindingHandle handle, @SuppressWarnings("exports") ServiceBindingSetup binding) {
        super(handle);
        this.binding = requireNonNull(binding);
    }

    /** {@return the domain this service is provided from.} */
    public ServiceNamespaceMirror namespace() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return whether or not the service is required.}
     * <p>
     * A service might not be required, for example, if it has a default value.
     */
    public boolean isRequired() {
        return binding.isRequired;
    }

    public boolean isResolved() {
        return binding.isResolved();
    }

    /**
     * A satisfiable binding is binding that is either resolved or not required.
     * <p>
     * By default building an application will fail if any service bindings are not satisfiable
     * @return
     */
    public boolean isSatisfiable() {
        return isResolved() || !isRequired();
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return binding.key;
    }

    // non null if resolvedx
    // Der er noget med en sti til servicen.
    public Optional<ProvidedServiceMirror> providingService() {
        throw new UnsupportedOperationException();
    }
}
//
//public Optional<BeanMirror> providedBy() {
//    throw new UnsupportedOperationException();
//}