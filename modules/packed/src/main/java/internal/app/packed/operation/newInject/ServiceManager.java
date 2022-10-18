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
package internal.app.packed.operation.newInject;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.service.DublicateServiceExportException;
import app.packed.service.DublicateServiceProvideException;
import app.packed.service.UnsatisfiableServiceDependencyException;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.BeanInstanceAccess;
import internal.app.packed.operation.OperationTarget.MethodOperationTarget;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.StringFormatter;

/**
 *
 */
public final class ServiceManager extends AbstractTreeNode<ServiceManager> {

    final LinkedHashMap<Key<?>, Entry> entries = new LinkedHashMap<>();

    // All provided services are automatically exported
    public boolean exportAll;

    public final LinkedHashMap<Key<?>, ExportedService> exports = new LinkedHashMap<>();

    public ServiceManager(@Nullable ServiceManager parent) {
        super(parent);
    }

    public ServiceBindingSetup serviceBind(Key<?> key, boolean isRequired, OperationSetup operation, int index) {
        return entries.compute(key, (k, v) -> {
            if (v == null) {
                v = new Entry(k);
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
            throw new DublicateServiceExportException();
        }
    }

    public void serviceExport(Key<?> key, OperationSetup operation) {
        serviceExport(new ExportedService(operation, key));
    }

    public ProvidedService serviceProvide(Key<?> key, OperationSetup bos) {
        Entry e = entries.compute(key, (k, v) -> {
            if (v == null) {
                v = new Entry(k);
            } else if (v.provider != null) {
                ProvidedService ps = v.provider;
                if (ps.operation.target instanceof BeanInstanceAccess) {
                    throw new DublicateServiceProvideException(
                            "Another bean of type " + ps.operation.bean.beanClass + " is already providing a service for Key<" + key + ">");
                } else if (ps.operation.target instanceof MethodOperationTarget m){
                    String ss = StringFormatter.formatShortWithParameters(m.method());
                    throw new DublicateServiceProvideException("A method " + ss + " is already providing a service for Key<" + key + ">");
                }
                throw new DublicateServiceProvideException("A service has already been bound for key " + key);
            } // else we have some bindings but no provider
            v.provider = new ProvidedService(bos, v);
            return v;
        });
        if (exportAll) {
            serviceExport(key, bos);
        }
        return e.provider;
    }

    public void verify() {
        for (Entry e : entries.values()) {
            if (e.provider == null) {
                for (var b = e.bindings; b != null; b = b.nextFriend) {
                    System.out.println("Binding not resolved " + b);
                }
                throw new UnsatisfiableServiceDependencyException();
            }
        }
    }

    public static class Entry {

        /** All bindings (in a interned linked list) that points to this entry. */
        @Nullable
        ServiceBindingSetup bindings;

        boolean isRequired = true; // true for now

        public final Key<?> key;

        /** The provider of the service */
        @Nullable
        public ProvidedService provider;

        Entry(Key<?> key) {
            this.key = requireNonNull(key);
        }
    }
}
