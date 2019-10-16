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
package packed.internal.service.build.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Function;

import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import app.packed.util.Key;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.run.MappingRuntimeEntry;
import packed.internal.service.run.RuntimeEntry;

/**
 * A build entry that that takes an existing entry and uses a {@link Function} to map the service provided by the entry.
 */
final class MappingBuildEntry<F, T> extends BuildEntry<T> {

    /** The entry that should be mapped. */
    final ServiceEntry<F> entryToMap;

    /** The function to apply on the */
    private final Function<? super F, T> function;

    MappingBuildEntry(ServiceExtensionNode node, ConfigSite configSite, ServiceEntry<F> entryToMap, Key<T> toKey, Function<F, T> function) {
        super(node, configSite, List.of());
        this.entryToMap = entryToMap;
        this.function = requireNonNull(function, "function is null");
        this.key = toKey;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return entryToMap.hasUnresolvedDependencies();
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return entryToMap.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode() {
        return new MappingRuntimeEntry<>(this, entryToMap, function);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return entryToMap.requiresPrototypeRequest();
    }
}
