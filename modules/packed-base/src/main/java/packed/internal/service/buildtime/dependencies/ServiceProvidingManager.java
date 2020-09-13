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
package packed.internal.service.buildtime.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.Provide;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.component.wirelet.WireletList;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.service.ProvideAllFromOtherInjector;
import packed.internal.service.runtime.AbstractInjector;

/**
 * This class manages everything to do with providing services for an {@link ServiceExtension}.
 *
 * @see ServiceExtension#provideAll(Injector, Wirelet...)
 * @see Provide
 */
public final class ServiceProvidingManager {

    /** All injectors added via {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
    private ArrayList<ProvideAllFromOtherInjector> provideAll;

    /** All explicit added build entries. */
    public final ArrayList<BuildtimeService<?>> buildEntries = new ArrayList<>();

    public void provideAll(InjectionManager im, AbstractInjector injector, ConfigSite configSite, WireletList wirelets) {
        ArrayList<ProvideAllFromOtherInjector> p = provideAll;
        if (provideAll == null) {
            p = provideAll = new ArrayList<>(1);
        }
        p.add(new ProvideAllFromOtherInjector(im, configSite, injector, wirelets));
    }

    public HashMap<Key<?>, BuildtimeService<?>> resolve(InjectionManager im) {
        LinkedHashMap<Key<?>, BuildtimeService<?>> resolvedServices = new LinkedHashMap<>();

        // First process provided entries, then any entries added via provideAll
        resolve0(im, resolvedServices, buildEntries);

        if (provideAll != null) {
            // All injectors have already had wirelets transform and filter
            for (ProvideAllFromOtherInjector fromInjector : provideAll) {
                resolve0(im, resolvedServices, fromInjector.entries.values());
            }
        }

        // Run through all linked containers...
        // Apply any wirelets to exports, and take

        // Add error messages if any nodes with the same key have been added multiple times
        return resolvedServices;
    }

    private void resolve0(InjectionManager im, LinkedHashMap<Key<?>, BuildtimeService<?>> resolvedServices,
            Collection<? extends BuildtimeService<?>> buildEntries) {
        for (BuildtimeService<?> entry : buildEntries) {
            BuildtimeService<?> existing = resolvedServices.putIfAbsent(entry.key(), entry);
            if (existing != null) {
                LinkedHashSet<BuildtimeService<?>> hs = im.errorManager().failingDuplicateProviders.computeIfAbsent(entry.key(), m -> new LinkedHashSet<>());
                hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                hs.add(entry);
            }
        }
    }
}
