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
package internal.app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.stream.Stream;

import app.packed.service.ProvidedServiceMirror;
import app.packed.service.ServiceBindingMirror;
import app.packed.service.UnsatisfiableDependencyException;
import app.packed.util.Key;
import app.packed.util.KeyAlreadyInUseException;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.util.StringFormatter;

/**
 * An entry in a service manager.
 */
public final class ServiceSetup {

    /** All bindings (in a interned linked list) that points to this entry. */
    @Nullable
    ServiceBindingSetup bindings;

    /** Used for checking for dependency cycles. */
    boolean hasBeenCheckForDependencyCycles;

    /** Whether or not the service is required. */
    boolean isRequired;

    /** The key of the entry. */
    public final Key<?> key;

    /** The single provider of the service. Can only be set once. */
    @Nullable
    private ServiceProviderSetup provider;

    ServiceSetup(Key<?> key) {
        this.key = requireNonNull(key);
    }

    ServiceBindingSetup bind(boolean isRequired, OperationSetup operation, int operationBindingIndex) {
        if (isRequired) {
            this.isRequired = true;
        }

        // Create the new binding
        ServiceBindingSetup binding = new ServiceBindingSetup(operation, operationBindingIndex, this, isRequired);

        // Add this binding to the list of bindings for the entry
        ServiceBindingSetup existing = bindings;
        if (existing == null) {
            bindings = binding;
        } else {
            existing.nextBinding = binding;
            bindings = binding;
        }
        return binding;
    }

    @Nullable
    public ServiceProviderSetup provider() {
        return provider;
    }

    ServiceProviderSetup setProvider(OperationSetup operation, BindingResolution resolution) {
        // Check if there is an existing provider for the same key, in which case we fail
        if (provider != null) {
            throw new KeyAlreadyInUseException(makeDublicateProvideErrorMsg(provider, operation));
        }

        // Create a new provider
        ServiceProviderSetup provider = this.provider = new ServiceProviderSetup(operation, this, resolution);

        operation.mirrorSupplier = () -> new ProvidedServiceMirror(provider);

        // add the service provider to the bean, this is used for cyclic dependency check later on
        operation.bean.serviceProviders.add(provider);

        return provider;

    }

    public Stream<ServiceBindingMirror> useSiteMirrors() {
        ArrayList<ServiceBindingMirror> l = new ArrayList<>();
        for (var b = bindings; b != null; b = b.nextBinding) {
            l.add((ServiceBindingMirror) b.mirror());
        }
        return l.stream();
    }

    /**
     *
     */
    public void verify() {
        if (provider == null) {
            for (var b = bindings; b != null; b = b.nextBinding) {
                System.out.println("Binding not resolved " + b);
            }
            throw new UnsatisfiableDependencyException("For key " + key);
        }
    }

    private String makeDublicateProvideErrorMsg(ServiceProviderSetup existingProvider, OperationSetup newOperation) {
        OperationSetup existingOperation = existingProvider.operation;

        // The same bean providing the same service
        if (existingOperation.bean == newOperation.bean) {
            return "This bean is already providing a service for " + key.toString() + ", beanClass = "
                    + StringFormatter.format(existingOperation.bean.beanClass);
        }

        if (existingProvider.resolution instanceof FromLifetimeArena) {
            return "Cannot provide a service for " + key.toString() + ", as another bean of type " + StringFormatter.format(existingOperation.bean.beanClass)
                    + " is already providing a service for the same key";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";
        } else if (existingProvider.resolution instanceof FromOperation os) {
            if (os.operation() instanceof MemberOperationSetup m && m.target instanceof OperationMethodTarget t) {
                String ss = StringFormatter.formatShortWithParameters(t.method());
                return "A method " + ss + " is already providing a service for " + key;
            }
        }
        return newOperation + "A service has already been bound for key " + key;
    }
}