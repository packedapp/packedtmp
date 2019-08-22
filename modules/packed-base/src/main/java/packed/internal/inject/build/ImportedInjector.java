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
import app.packed.container.WireletList;
import app.packed.util.Key;
import packed.internal.inject.build.wirelets.ServiceWirelet;
import packed.internal.inject.run.AbstractInjector;

/** Provides services from an existing Injector. */
public final class ImportedInjector {

    /** The injector builder into which the injector is imported. */
    final InjectorBuilder builder;

    /** The configuration site of the import statement. */
    public final ConfigSite configSite;

    public final LinkedHashMap<Key<?>, BSEImported<?>> entries = new LinkedHashMap<>();

    /** The injector to import from. */
    final AbstractInjector injector;

    /** Any wirelets used when importing the injector. */
    final WireletList wirelets;

    /**
     * @param builder
     *            the injector builder
     * @param configSite
     * @param injector
     * @param wirelets
     */
    ImportedInjector(InjectorBuilder builder, ConfigSite configSite, AbstractInjector injector, WireletList wirelets) {
        this.builder = builder;
        this.configSite = requireNonNull(configSite);
        this.injector = requireNonNull(injector);
        this.wirelets = requireNonNull(wirelets);

        injector.forEachServiceEntry(e -> {
            if (!e.isPrivate()) { // ignores Injector, and other
                entries.put(e.key(), new BSEImported<>(this, e));
            }
        });

        wirelets.forEach(ServiceWirelet.class, w -> w.apply(this));
    }

    //
    // // Add all to the private node map
    // // This should not be done here....
    // for (BSE<?> node : entries.values()) {
    // if (!builder.resolver.internalNodes.putIfAbsent(node)) {
    // throw new InjectionException("oops for " + node.key()); // Tried to import a service with a key that was already
    // present
    // }
    // }

}
