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

import app.packed.binding.Key;
import app.packed.util.Nullable;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class ExportedService {

    /** The key under which the service is exported */
    public final Key<?> key;

    /** The operation that exports the service. */
    public final OperationSetup operation;

    /** A route to where the service is actually located. Null indicates directly from the operation */
    @Nullable
    public final ServicePath path;

    ExportedService(OperationSetup os, Key<?> key) {
        this.operation = os;
        this.key = key;
        this.path = null;
    }
}
