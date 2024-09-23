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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceMirror;
import app.packed.service.mirror.NamespaceServiceBindingMirror;
import app.packed.service.mirror.ServiceProviderMirror;
import internal.app.packed.service.ServiceNamespaceHandle;

/**
 * A mirror for a service namespace.
 */
public final class ServiceNamespaceMirror extends NamespaceMirror<BaseExtension> {

    /**
     * Creates a new service namespace mirror.
     *
     * @param handle
     *            the namespace's handle
     *
     * @implNote Invoked via
     *           {@link internal.app.packed.handlers.ServiceHandlers#newServiceNamespaceMirror(ServiceNamespaceHandle)}
     */
    ServiceNamespaceMirror(ServiceNamespaceHandle handle) {
        super(handle);
    }

    /** {@return A map of all the bindings from providers in the namespace} */
    public Map<Key<?>, Collection<NamespaceServiceBindingMirror>> bindings() {
        throw new UnsupportedOperationException();
    }

    /** {@return keys for every service that is available in the namespace.} */
    public Set<Key<?>> keys() {
        return providers().keySet();
    }

    /** {@return a map with details of all the provided services in the namespace.} */
    public Map<Key<?>, ServiceProviderMirror> providers() {
        throw new UnsupportedOperationException();
    }

    // required? <- Incoming
    // Det er services
}
