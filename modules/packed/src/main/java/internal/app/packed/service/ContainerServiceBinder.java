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

import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.service.ServiceContract;
import internal.app.packed.service.ServiceManagerRequirementsSetup.Requirement;
import internal.app.packed.service.runtime.AbstractServiceLocator;
import internal.app.packed.service.sandbox.Service;

/**
 *
 */
public final class ContainerServiceBinder {

    final InternalServiceExtension cim;

    /** Deals with everything about exporting services to a parent container. */
    private ServiceManagerExportSetup exports;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    private ServiceManagerRequirementsSetup requirements;

    ContainerServiceBinder(InternalServiceExtension cim) {
        this.cim = requireNonNull(cim);
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    @Nullable
    public ServiceManagerExportSetup exports() {
        return exports;
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public ServiceManagerExportSetup exportsOrCreate() {
        ServiceManagerExportSetup r = exports;
        if (r == null) {
            r = exports = new ServiceManagerExportSetup(cim);
        }
        return r;
    }

    public boolean hasExports() {
        return exports != null;
    }

    public boolean hasRequirements() {
        return requirements != null;
    }

    /** {@return a service contract for this manager.} */
    public ServiceContract newServiceContract() {
        ServiceContract.Builder builder = ServiceContract.builder();

        cim.container.sm.exports.keySet().forEach(k -> builder.provide(k));

        // Add requirements (mandatory or optional)
        if (requirements != null && requirements.requirements != null) {
            for (Requirement r : requirements.requirements.values()) {
                if (r.isOptional) {
                    builder.requireOptional(r.key);
                } else {
                    builder.require(r.key);
                }
            }
        }

        return builder.build();
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    @Nullable
    public ServiceManagerRequirementsSetup requirements() {
        return requirements;
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public ServiceManagerRequirementsSetup requirementsOrCreate() {
        ServiceManagerRequirementsSetup r = requirements;
        if (r == null) {
            r = requirements = new ServiceManagerRequirementsSetup();
        }
        return r;
    }

    /** A service locator wrapping all exported services. */
    static final class ExportedServiceLocator extends AbstractServiceLocator {

        /** All services that this injector provides. */
        private final Map<Key<?>, ? extends Service> services;

        ExportedServiceLocator(Map<Key<?>, ? extends Service> services) {
            this.services = requireNonNull(services);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public Map<Key<?>, Service> asMap() {
            // as() + addAttribute on all services is disabled before we start the
            // export process. So ServiceBuild can be considered as effectively final
            return (Map) services;
        }

        @Override
        protected String useFailedMessage(Key<?> key) {
            // /child [ss.BaseMyAssembly] does not export a service with the specified key

            // FooAssembly does not export a service with the key
            // It has an internal service. Maybe you forgot to export it()
            // Is that breaking encapsulation
            // container.realm().realmType();
            return "A service with the specified key, key = " + key;
        }
    }
}
