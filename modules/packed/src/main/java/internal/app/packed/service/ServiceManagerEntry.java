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

import app.packed.base.Key;
import app.packed.base.Nullable;

/**
 * An entry in a service manager.
 */
public final class ServiceManagerEntry {

    /** All bindings (in a interned linked list) that points to this entry. */
    @Nullable
    ServiceBindingSetup bindings;

    boolean isRequired = true; // true for now

    /** The key of the entry. */
    public final Key<?> key;

    /** Used for checking for dependency cycles. */
    boolean needsPostProcessing = true;

    /** The single provider of the service. */
    @Nullable
    public ProvidedService provider;

    ServiceManagerEntry(Key<?> key) {
        this.key = requireNonNull(key);
    }
}