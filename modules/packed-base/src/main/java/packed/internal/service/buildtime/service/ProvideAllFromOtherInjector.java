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
package packed.internal.service.buildtime.service;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.component.wirelet.WireletList;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.buildtime.wirelets.PackedUpstreamInjectionWirelet;
import packed.internal.service.runtime.AbstractInjector;
import packed.internal.service.runtime.RuntimeEntry;
import packed.internal.util.KeyBuilder;

/** Represents an injector that used via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
public final class ProvideAllFromOtherInjector {

    /** The injector builder from where the service will be provided. */
    public final InjectionManager node;

    /** The configuration site of the provide all statement. */
    public final ConfigSite configSite;

    /** All entries that was imported, any wirelets that was specified when importing the injector may modify this map. */
    // Is not ProvideABE because we might transform some of the entries...
    public final LinkedHashMap<Key<?>, BuildEntry<?>> entries = new LinkedHashMap<>();

    /** The injector that provides the services. */
    final AbstractInjector injector;

    /**
     * Creates a new instance.
     * 
     * @param node
     *            A builder for the injector that the injector is being imported into
     * @param configSite
     *            the config site of the import
     * @param injector
     *            the injector that is being imported
     * @param wirelets
     *            any wirelets used when importing the injector
     */
    @SuppressWarnings("unchecked")
    public ProvideAllFromOtherInjector(InjectionManager node, ConfigSite configSite, AbstractInjector injector, WireletList wirelets) {
        this.node = requireNonNull(node);
        this.configSite = requireNonNull(configSite);
        this.injector = requireNonNull(injector);

        injector.forEachEntry(e -> {
            if (!e.key().equals(KeyBuilder.INJECTOR_KEY)) { // ignores Injector, and other
                entries.put(e.key(), new FromOtherInjectorBuildEntry<>(this, (RuntimeEntry<Object>) e));
            }
        });

        // process wirelets for filtering/transformations
        wirelets.forEach(PackedUpstreamInjectionWirelet.class, w -> w.process(this));
    }
}
