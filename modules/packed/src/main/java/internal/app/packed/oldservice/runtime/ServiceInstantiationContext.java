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
package internal.app.packed.oldservice.runtime;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;

import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.oldservice.build.ServiceSetup;

/** A special instantiation context that is created */
// Vi beholder den lidt endnu, det saa traels hver gang vi
// laver aendringer, hvis vi bare sender Region+IHM rundt
public final class ServiceInstantiationContext {

    public final LifetimeObjectArena pool;

    public final IdentityHashMap<ServiceSetup, RuntimeService> transformers = new IdentityHashMap<>();

    public ServiceInstantiationContext() {
        pool = null;
    }

    public ServiceInstantiationContext(LifetimeObjectArena pool) {
        this.pool = requireNonNull(pool);
    }
}
