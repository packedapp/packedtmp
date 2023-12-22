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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import app.packed.service.UnsatisfiableDependencyException;
import app.packed.service.mirror.ServiceBindingMirror;
import app.packed.service.mirror.oldMaybe.ProvidedServiceMirror;
import app.packed.util.Key;
import app.packed.util.KeyAlreadyUsedException;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperationResult;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.util.StringFormatter;

/**
 * An entry in a service manager.
 */
public final class ServiceSetup {

    /** All bindings (in a interned linked list) that points to this entry. */
    final ArrayList<ServiceBindingSetup> bindings = new ArrayList<>();

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

    public List<ServiceBindingSetup> removeBindingsForBean(BeanSetup bean) {
        boolean isRequired = false;
        ArrayList<ServiceBindingSetup> l = new ArrayList<>();
        for (Iterator<ServiceBindingSetup> iterator = bindings.iterator(); iterator.hasNext();) {
            ServiceBindingSetup s = iterator.next();
            if (s.operation.bean == bean) {
                l.add(s);
                iterator.remove();
            } else {
                isRequired |= s.isRequired;
            }
        }
        if (provider == null && bindings.isEmpty()) {
            bean.container.sm.entries.remove(key);
        } else {
            this.isRequired = isRequired;
        }
        return l;
    }

    ServiceBindingSetup bind(boolean isRequired, OperationSetup operation, int operationBindingIndex) {
        if (isRequired) {
            this.isRequired = true;
        }

        // Create the new binding
        ServiceBindingSetup binding = new ServiceBindingSetup(operation, operationBindingIndex, this, isRequired);

        bindings.add(binding);
        return binding;
    }

    @Nullable
    public ServiceProviderSetup provider() {
        return provider;
    }

    ServiceProviderSetup setProvider(OperationSetup operation, BindingResolution resolution) {
        // Check if there is an existing provider for the same key, in which case we fail
        if (provider != null) {
            throw new KeyAlreadyUsedException(makeDublicateProvideErrorMsg(provider, operation));
        }

        // Create a new provider
        ServiceProviderSetup p = provider = new ServiceProviderSetup(operation, this, resolution);

        operation.mirrorSupplier = () -> new ProvidedServiceMirror(p);

        // add the service provider to the bean, this is used for cyclic dependency check later on
        operation.bean.operations.serviceProviders.add(p);

        return p;

    }

    public Stream<ServiceBindingMirror> useSiteMirrors() {
        ArrayList<ServiceBindingMirror> l = new ArrayList<>();
        for (ServiceBindingSetup b : bindings) {
            l.add((ServiceBindingMirror) b.mirror());
        }
        return l.stream();
    }

    /**
     *
     */
    public void verify() {
        if (provider == null) {
            for (ServiceBindingSetup b : bindings) {
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
        } else if (existingProvider.resolution instanceof FromOperationResult os) {
            if (os.operation() instanceof MemberOperationSetup m && m.target instanceof OperationMethodTarget t) {
                String ss = StringFormatter.formatShortWithParameters(t.method());
                return "A method " + ss + " is already providing a service for " + key;
            }
        }
        return newOperation + " A service has already been bound for key " + key;
    }
}