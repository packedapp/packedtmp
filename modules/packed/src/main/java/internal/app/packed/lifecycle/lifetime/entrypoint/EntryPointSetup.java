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
package internal.app.packed.lifecycle.lifetime.entrypoint;

import internal.app.packed.lifecycle.lifetime.LifetimeSetup;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
// For now entry points in beans with bean lifetime is not supported

public final class EntryPointSetup {

    /** The lifetime the entry point creates. */
    public final LifetimeSetup lifetime;

    /** The operation of this entry point. */
    public final OperationSetup operation;

    // Har vi brug for en counter, Kan vi smide den paa lifetime setup

    public EntryPointSetup(OperationSetup operation, LifetimeSetup lifetime) {
        this.lifetime = lifetime;
        this.operation = operation;
    }
}
