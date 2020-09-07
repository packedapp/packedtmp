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
package packed.internal.service.buildtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;

import packed.internal.component.Region;
import packed.internal.service.buildtime.service.ComponentFactoryBuildEntry;
import packed.internal.service.buildtime.service.ServiceProvidingManager;
import packed.internal.service.runtime.RuntimeEntry;

/** A special instantiation context that is created */
public class ServiceExtensionInstantiationContext {

    public final Region ns;

    public final ArrayDeque<ComponentFactoryBuildEntry<?>> entriesToInstantiate = new ArrayDeque<>();

    public ServiceProvidingManager spm;

    ServiceExtensionInstantiationContext(Region ns) {
        this.ns = requireNonNull(ns);
    }

    // Translates from BuildEntry->RuntimeEntry
    final IdentityHashMap<BuildEntry<?>, RuntimeEntry<?>> transformers = new IdentityHashMap<>();

    final IdentityHashMap<BuildEntry<?>, MethodHandle> handlers = new IdentityHashMap<>();
}
