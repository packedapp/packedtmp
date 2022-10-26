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

import java.lang.invoke.MethodHandle;

import internal.app.packed.operation.OperationSetup;

/**
 *
 */
// IDK
public final class ProvidedService {

    /** The operation that provides the service. */
    public final OperationSetup operation;

    /** The key under which this service is provided. */
    public final ServiceManagerEntry entry;

    public MethodHandle provider;

    public final boolean isConstant;

    ProvidedService(OperationSetup operation, boolean isConstant, ServiceManagerEntry entry) {
        this.operation = operation;
        this.entry = entry;
        this.isConstant = isConstant;
    }
}
