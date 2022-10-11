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

import java.util.LinkedHashMap;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.service.DublicateServiceExportException;
import app.packed.service.UnsatisfiableServiceDependencyException;
import internal.app.packed.operation.BeanOperationSetup;

/**
 *
 */
public class ServiceManager {

    final LinkedHashMap<Key<?>, Entry> entries = new LinkedHashMap<>();

    // All provided services are automatically exported
    boolean exportAll;

    final LinkedHashMap<Key<?>, ExportedService> exports = new LinkedHashMap<>();

    public void add(ExportedService e) {
        ExportedService existing = exports.putIfAbsent(e.key, e);
        if (existing == null) {
            // A service with the key has already been exported
            throw new DublicateServiceExportException();
        }
    }

    public ServiceBindingSetup addBinding(Key<?> key, boolean isRequired, BeanOperationSetup operation, int index) {
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

    public ProvidedService addProvision(Key<?> key, BeanOperationSetup bos) {
        return entries.compute(key, (k, v) -> {
            if (v == null) {
                v = new Entry(k);
            } else if (v.provider != null) {
                throw new RuntimeException();
            } // else we have some bindings but no provider
            v.provider = new ProvidedService(bos, v);
            return v;
        }).provider;
    }

    public void verify() {
        for (Entry e : entries.values()) {
            if (e.provider == null) {
                // okay we do not provide it internally in the container
                
                throw new UnsatisfiableServiceDependencyException();
            }
        }
    }

    static class Entry {
        
        @Nullable
        ServiceBindingSetup bindings;

        boolean isRequired = true; // true for now

        final Key<?> key;

        @Nullable
        ProvidedService provider;

        Entry(Key<?> key) {
            this.key = key;
        }
    }
}
