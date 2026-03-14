/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import app.packed.extension.OverviewHandle;
import app.packed.extension.OverviewMirror;
import app.packed.service.mirror.ServiceBindingMirror;
import app.packed.service.mirror.ServiceProviderMirror;
import app.packed.service.mirrorold.ExportedServiceMirror;
import internal.app.packed.extension.PackedExtensionHandle;

/**
 * An overview mirror for services provided by {@link BaseExtension}.
 */
public final class ServiceOverviewMirror extends OverviewMirror<BaseExtension> {

    ServiceOverviewMirror(OverviewHandle<BaseExtension> handle) {
        super(handle);
    }

    /** {@return A map of all the bindings from providers in the namespace} */
    public Map<Key<?>, Collection<ServiceBindingMirror>> bindings() {
        throw new UnsupportedOperationException();
    }

    /** {@return keys for every service that is available in the namespace.} */
    public Set<Key<?>> keys() {
        return providers().keySet();
    }

    /** {@return a map with details of all the provided services in the namespace.} */
    public Map<Key<?>, ServiceProviderMirror.FromNamespace> providers() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return a service contract for the container}
     * <p>
     * If the configuration of the container has not been completed. This method return a contract on a best effort basis.
     */
    public ServiceContract serviceContract() {
        return extensionHandle().extension().container.servicesMain().newContract();
    }

    /** {@return a map of all the services that are exported by the container.} */
    @SuppressWarnings("exports")
    public Map<Key<?>, ExportedServiceMirror> serviceExports() {
        return extensionHandle().extension().container.servicesMain().exports.toUnmodifiableSequenceMap(e -> (ExportedServiceMirror) e.operation.mirror());
    }

    /** {@return the internal extension handle for accessing service internals.} */
    private PackedExtensionHandle<BaseExtension> extensionHandle() {
        return (PackedExtensionHandle<BaseExtension>) handle().applicationRootHandle();
    }
}
