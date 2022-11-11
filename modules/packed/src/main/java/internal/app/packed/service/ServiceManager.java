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

import java.util.LinkedHashMap;

import app.packed.framework.Nullable;
import app.packed.service.ExportedServiceCollisionException;
import app.packed.service.ExportedServiceMirror;
import app.packed.service.Key;
import app.packed.service.ProvidedServiceCollisionException;
import app.packed.service.ProvidedServiceMirror;
import app.packed.service.UnsatisfiableServiceDependencyException;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSite;
import internal.app.packed.operation.OperationSite.LifetimePoolAccessSite;
import internal.app.packed.operation.OperationSite.MethodOperationSite;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.StringFormatter;

/**
 *
 */
public final class ServiceManager extends AbstractTreeNode<ServiceManager> {

    public final LinkedHashMap<Key<?>, ServiceManagerEntry> entries = new LinkedHashMap<>();

    // All provided services are automatically exported
    public boolean exportAll;

    /** Exported services from the container. */
    public final LinkedHashMap<Key<?>, ExportedService> exports = new LinkedHashMap<>();

    public ServiceManager(@Nullable ServiceManager parent) {
        super(parent);
    }

    private String makeDublicateProvideErrorMsg(ProvidedService provider, OperationSetup otherOperation) {
        OperationSite existingTarget = provider.operation.site;
        OperationSite thisTarget = otherOperation.site;

        Key<?> key = provider.entry.key;

        if (existingTarget.bean == thisTarget.bean) {
            return "This bean is already " + existingTarget.bean.beanClass + " is already providing a service for Key<" + key.toStringSimple() + ">";
        }
        if (existingTarget instanceof LifetimePoolAccessSite) {
            return "Another bean of type " + existingTarget.bean.beanClass + " is already providing a service for Key<" + key.toStringSimple() + ">";
        } else if (existingTarget instanceof MethodOperationSite m) {
            String ss = StringFormatter.formatShortWithParameters(m.method());
            return "A method " + ss + " is already providing a service for Key<" + key + ">";
        }
        return thisTarget + "A service has already been bound for key " + key;
    }

    /**
     * Provides a service from the specified operation.
     * 
     * @param key
     *            the key for which to provide a service for
     * @param operation
     *            the operation that provides the service
     * @return a provided service
     */
    public ProvidedService provideService(Key<?> key, boolean isConstant, OperationSetup operation) {
        ServiceManagerEntry entry = entries.computeIfAbsent(key, ServiceManagerEntry::new);

        // Check lifetimes

        // Get any existing provider of
        ProvidedService provider = entry.provider;

        // Fail if there is already another provider of a service with key
        if (provider != null) {
            throw new ProvidedServiceCollisionException(makeDublicateProvideErrorMsg(provider, operation));
        }

        // Create a new provider
        entry.provider = provider = new ProvidedService(operation, isConstant, entry);

        operation.mirrorSupplier = () -> new ProvidedServiceMirror(entry.provider);

        // add the service provider to the bean
        operation.site.bean.operationsProviders.add(provider);

        if (exportAll) {
            serviceExport(key, operation);
        }

        // maintain old
        operation.site.bean.container.injectionManager.provideService(provider);

        return provider;
    }

    public ServiceBindingSetup serviceBind(Key<?> key, boolean isRequired, OperationSetup operation, int index) {
        return entries.compute(key, (k, v) -> {
            if (v == null) {
                v = new ServiceManagerEntry(k);
            }
            if (isRequired) {
                v.isRequired = true;
            }
            ServiceBindingSetup sbs = new ServiceBindingSetup(operation, index, v, isRequired);
            ServiceBindingSetup existing = v.bindings;
            if (existing == null) {
                v.bindings = sbs;
            } else {
                existing.nextFriend = sbs;
                v.bindings = sbs;
            }
            return v;
        }).bindings;
    }

    public void serviceExport(ExportedService e) {
        ExportedService existing = exports.putIfAbsent(e.key, e);
        if (existing != null) {
            // A service with the key has already been exported
            throw new ExportedServiceCollisionException("Jmm");
        }
        e.bos.mirrorSupplier = () -> new ExportedServiceMirror(e);
    }

    public void serviceExport(Key<?> key, OperationSetup operation) {
        serviceExport(new ExportedService(operation, key));
    }

    public void verify() {
        for (ServiceManagerEntry e : entries.values()) {
            if (e.provider == null) {
                for (var b = e.bindings; b != null; b = b.nextFriend) {
                    System.out.println("Binding not resolved " + b);
                }
                throw new UnsatisfiableServiceDependencyException("For key " + e.key);
            }
        }
    }
}
