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

import java.util.LinkedHashMap;

import app.packed.config.ConfigSite;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.inject.InjectionExtension;
import app.packed.inject.Injector;
import app.packed.util.Key;
import packed.internal.inject.run.AbstractInjector;

/** Represents an injector that was imported via {@link InjectionExtension#importAll(Injector, Wirelet...)}. */
public final class ImportedInjector {

    /** The injector builder into which the injector is imported. */
    final InjectorBuilder builder;

    /** The configuration site of the import statement. */
    public final ConfigSite configSite;

    /** All entries that was imported, any wirelets that was specified when importing the injector may modify this map. */
    public final LinkedHashMap<Key<?>, BSEImported<?>> entries = new LinkedHashMap<>();

    /** The injector to import from. */
    final AbstractInjector injector;

    /**
     * Creates a new instance.
     * 
     * @param builder
     *            A builder for the injector that the injector is being imported into
     * @param configSite
     *            the config site of the import
     * @param injector
     *            the injector that is being imported
     * @param wirelets
     *            any wirelets used when importing the injector
     */
    ImportedInjector(InjectorBuilder builder, ConfigSite configSite, AbstractInjector injector, WireletList wirelets) {
        this.builder = requireNonNull(builder);
        this.configSite = requireNonNull(configSite);
        this.injector = requireNonNull(injector);

        injector.forEachServiceEntry(e -> {
            if (!e.isPrivate()) { // ignores Injector, and other
                entries.put(e.key(), new BSEImported<>(this, e));
            }
        });

        // process wirelets for filtering/transformations
        wirelets.forEach(PackedUpstreamServiceWirelet.class, w -> w.process(this));
    }
}
