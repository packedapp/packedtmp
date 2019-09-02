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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvideHelper;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.run.RSE;
import packed.internal.inject.run.RSEDelegate;

/** A build node that imports a service from another injector. */
public class BSEImported<T> extends BSE<T> {

    /** The node to import. */
    public final ServiceEntry<T> importedEntry;

    /** The bind injector source. */
    public final ProvideAllFromInjector importedInjector;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    BSEImported(ProvideAllFromInjector importedInjector, ServiceEntry<T> importedEntry) {
        super(importedInjector.builder, importedInjector.configSite.withParent(importedEntry.configSite()), List.of());
        this.importedEntry = requireNonNull(importedEntry);
        this.importedInjector = requireNonNull(importedInjector);
        this.as((Key) importedEntry.key());
        this.description = importedEntry.description().orElse(null);
    }

    @Override
    @Nullable
    public BSE<?> declaringNode() {
        return (importedEntry instanceof BSE) ? ((BSE<?>) importedEntry).declaringNode() : null;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideHelper site) {
        return importedEntry.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return importedEntry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return importedEntry.needsInjectionSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return importedEntry.needsResolving();
    }

    /** {@inheritDoc} */
    @Override
    RSE<T> newRuntimeNode() {
        return new RSEDelegate<T>(this, importedEntry);
    }
}
