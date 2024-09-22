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

import app.packed.binding.Key;
import app.packed.container.Wirelet;

/**
 *
 */

// Provide instances() (Runtime) // I think they must match a service...

// Transform Incoming, Outgoing  (not runtime)
// Contract check (Runtime) <- IDK hmm, Altsaa vel ikke hvis vi ikke har en service locator
// Anchoring <- Save services in the container that is used directly by the container

// provideInstance/provideConstant/provide (if transformers have provideConstant I think we should here as well)
// transform
// contract
// anchor

// exportTransitive

public class ServiceWirelets {

    public static <T> Wirelet provideInstance(Key<T> key, T instance) {
       throw new UnsupportedOperationException();
    }
}
