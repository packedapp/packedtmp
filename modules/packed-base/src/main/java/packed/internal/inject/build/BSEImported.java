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
import packed.internal.config.site.InternalConfigSite;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.run.RSE;
import packed.internal.inject.run.RSEDelegate;

/** A build node that imports a service from another injector. */
class BSEImported<T> extends BSE<T> {

    /** The node to import. */
    final ServiceEntry<T> importFrom;

    /** The bind injector source. */
    final ImportedInjector injectorImporter;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    BSEImported(InjectorBuilder injectorConfiguration, InternalConfigSite configSite, ImportedInjector injectorToImportFrom, ServiceEntry<T> importFrom) {
        super(injectorConfiguration, configSite, List.of());
        this.importFrom = requireNonNull(importFrom);
        this.injectorImporter = requireNonNull(injectorToImportFrom);
        this.as((Key) importFrom.key());
        this.description = importFrom.description().orElse(null);
    }

    @Override
    @Nullable
    public BSE<?> declaringNode() {
        return (importFrom instanceof BSE) ? ((BSE<?>) importFrom).declaringNode() : null;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideHelper site) {
        return importFrom.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return importFrom.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return importFrom.needsInjectionSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return importFrom.needsResolving();
    }

    /** {@inheritDoc} */
    @Override
    RSE<T> newRuntimeNode() {
        return new RSEDelegate<T>(this, importFrom);
    }
}
