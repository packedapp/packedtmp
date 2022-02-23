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
package packed.internal.inject.service;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.inject.service.ServiceContract;
import packed.internal.inject.service.ServiceManagerRequirementsSetup.Requirement;
import packed.internal.inject.service.build.ServiceSetup;

/**
 *
 */
public class InputOutputServiceManager {

    final ContainerInjectionManager cim;

    /** Deals with everything about exporting services to a parent container. */
    private ServiceManagerExportSetup exports;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    private ServiceManagerRequirementsSetup requirements;

    InputOutputServiceManager(ContainerInjectionManager cim) {
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
            r = exports = new ServiceManagerExportSetup(null);
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

        // Add exports
        if (exports != null) {
            for (ServiceSetup n : exports) {
                builder.provide(n.key());
            }
        }

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
}
