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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.packed.base.Key;
import app.packed.inject.Service;
import app.packed.inject.ServiceRegistry;
import packed.internal.inject.service.assembly.ServiceAssembly;
import packed.internal.util.PackedAttributeHolderStream;

/**
 *
 */
public final class PackedServiceRegistry implements ServiceRegistry {

    /** The services that are wrapped */
    private final List<Service> services;

    public PackedServiceRegistry(List<Service> services) {
        this.services = requireNonNull(services);
    }

    /** {@inheritDoc} */
    @Override
    public PackedAttributeHolderStream<Service> stream() {
        return new PackedAttributeHolderStream<>(services.stream());
    }

    @Override
    public String toString() {
        return services.toString();
    }

    public static PackedServiceRegistry of(Map<Key<?>, ? extends ServiceAssembly<?>> map) {
        List<Service> l = new ArrayList<>();
        for (ServiceAssembly<?> e : map.values()) {
            l.add(e.toService());
        }
        return new PackedServiceRegistry(l);
    }
}
