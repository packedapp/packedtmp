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

import java.util.ArrayList;

import app.packed.config.ConfigSite;
import app.packed.inject.InjectionExtension;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;

/** This class takes care of everything related to exported entries. */
public final class InjectorBuilderExporter {

    /**
     * All nodes that have been exported, typically via {@link InjectionExtension#export(Class)},
     * {@link InjectionExtension#export(Key)} or {@link InjectionExtension#export(ProvidedComponentConfiguration)}.
     */
    public final ArrayList<BSEExported<?>> exportedEntries = new ArrayList<>();

    /** The builder this exporter belongs to. */
    private final InjectorBuilder builder;

    /**
     * Creates a new exported
     * 
     * @param builder
     *            the builder this exporter belongs to
     */
    InjectorBuilderExporter(InjectorBuilder builder) {
        this.builder = requireNonNull(builder);
    }

    /**
     * Registers the specified key to be exported.
     * 
     * @param <T>
     *            the type of service
     * @param key
     *            the key of the service to export
     * @param configSite
     *            the config site of the export
     * @return a service configuration that can be returned to the user
     */
    public <T> ServiceConfiguration<T> export(Key<T> key, ConfigSite configSite) {
        return export0(new BSEExported<>(builder, configSite, key));
    }

    public <T> ServiceConfiguration<T> export(ProvidedComponentConfiguration<T> configuration, ConfigSite configSite) {
        PackedProvidedComponentConfiguration<T> ppcc = (PackedProvidedComponentConfiguration<T>) configuration;
        if (ppcc.buildEntry.injectorBuilder != builder) {
            throw new IllegalArgumentException("The specified configuration object was created by another injector extension instance");
        }
        return export0(new BSEExported<>(builder, configSite, ppcc.buildEntry));
    }

    /**
     * Converts the internal exported entry to a service configuration object.
     * 
     * @param <T>
     *            the type of service the entry wraps
     * @param entry
     *            the entry to convert
     * @return a service configuration object
     */
    private <T> ServiceConfiguration<T> export0(BSEExported<T> entry) {
        exportedEntries.add(entry);
        return entry.toServiceConfiguration();
    }

    public void exportAll(ConfigSite configSite) {
        // Add exportAll(Predicate); //Maybe some exportAll(Consumer<ExportedConfg>)
        // any exportAll can be called at most one
        // Can be called at any time
        // explicit single exports will override any exportedAll. But aliases are allowed
        // transient linked exports, will work regardless
        throw new UnsupportedOperationException();
    }
}
