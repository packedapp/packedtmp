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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;

import packed.internal.component.RuntimeRegion;
import packed.internal.inject.service.assembly.ServiceAssembly;

/** A special instantiation context that is created */
// Vi beholder den lidt endnu, det saa traels hver gang vi
// laver aendringer, hvis vi bare sender Region+IHM rundt
public class ServiceInstantiationContext {

    public final RuntimeRegion region;

    // Translates from BuildEntry->RuntimeEntry
    public final IdentityHashMap<ServiceAssembly<?>, RuntimeService<?>> transformers = new IdentityHashMap<>();

    public ServiceInstantiationContext(RuntimeRegion ns) {
        this.region = requireNonNull(ns);
    }
}
