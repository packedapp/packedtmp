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
package packed.internal.service.buildtime.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.base.Key;
import app.packed.config.ConfigSite;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.dependencies.InjectionManager;
import packed.internal.service.runtime.MappingInjectorEntry;
import packed.internal.service.runtime.RuntimeService;

/**
 * A build entry that that takes an existing entry and uses a {@link Function} to map the service provided by the entry.
 */
final class MappingBuildEntry<F, T> extends BuildtimeService<T> {

    /** The entry that should be mapped. */
    final BuildtimeService<F> entryToMap;

    /** The function to apply on the */
    private final Function<? super F, T> function;

    MappingBuildEntry(InjectionManager node, ConfigSite configSite, BuildtimeService<F> entryToMap, Key<T> toKey, Function<F, T> function) {
        super(node, configSite, toKey);
        this.entryToMap = entryToMap;
        this.function = requireNonNull(function, "function is null");
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new MappingInjectorEntry<>(this, entryToMap.toRuntimeEntry(context), function);
    }
}
