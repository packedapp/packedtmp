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
package packed.internal.inject.build.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.config.ConfigSite;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceRequest;
import app.packed.util.Key;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.run.MappingRunEntry;
import packed.internal.inject.run.RSE;

/**
 *
 */
final class MappingBuildEntry<F, T> extends BuildEntry<T> {

    final ServiceEntry<F> entryToMap;

    private final Function<F, T> function;

    MappingBuildEntry(InjectorBuilder injectorBuilder, ServiceEntry<F> entryToMap, Key<T> toKey, Function<F, T> function, ConfigSite configSite) {
        super(injectorBuilder, configSite);
        this.entryToMap = entryToMap;
        this.function = requireNonNull(function, "function is null");
        this.key = toKey;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return entryToMap.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ServiceRequest site) {
        // Null check..
        F instance = entryToMap.getInstance(site);
        return function.apply(instance);
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return entryToMap.needsInjectionSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return entryToMap.needsResolving();
    }

    /** {@inheritDoc} */
    @Override
    protected RSE<T> newRuntimeNode() {
        return new MappingRunEntry<>(this, entryToMap, function);
    }
}
