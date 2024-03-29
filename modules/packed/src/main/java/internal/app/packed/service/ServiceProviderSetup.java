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

import internal.app.packed.binding.BindingResolution;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class ServiceProviderSetup {

    /** The service manager entry. */
    public final ServiceSetup entry;

    /** The operation that provides the service. */
    public final OperationSetup operation;

    /** How the service is provided. */
    public final BindingResolution resolution;

    ServiceProviderSetup(OperationSetup operation, ServiceSetup entry, BindingResolution resolution) {
        this.operation = requireNonNull(operation);
        this.entry = requireNonNull(entry);
        this.resolution = requireNonNull(resolution);
    }
}
