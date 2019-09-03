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
package packed.internal.inject.build.export;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import app.packed.config.ConfigSite;
import app.packed.inject.InjectionExtension;
import app.packed.inject.InjectorContract;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BSE;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.build.PackedProvidedComponentConfiguration;
import packed.internal.inject.compose.InjectorResolver;
import packed.internal.inject.util.ServiceNodeMap;
import packed.internal.util.StringFormatter;

/** This class takes care of everything related to exported entries. */
public final class ServiceExporter {

    /** The injector builder this exporter belongs to. */
    private final InjectorBuilder builder;

    /**
     * All nodes that have been exported, typically via {@link InjectionExtension#export(Class)},
     * {@link InjectionExtension#export(Key)} or {@link InjectionExtension#export(ProvidedComponentConfiguration)}.
     */
    public final ArrayList<ExportedBuildEntry<?>> exports = new ArrayList<>();

    /** */
    public final ServiceNodeMap resolvedExports = new ServiceNodeMap();

    /** A map of multiple exports for the same key. */
    @Nullable
    public Map<Key<?>, ArrayList<ExportedBuildEntry<?>>> exportsDuplicates;

    public HashMap<Key<?>, HashSet<BSE<?>>> unresolvedExports = new HashMap<>();

    /**
     * Creates a new exporter.
     * 
     * @param builder
     *            the builder this exporter belongs to
     */
    public ServiceExporter(InjectorBuilder builder) {
        this.builder = requireNonNull(builder);
    }

    public void buildContract(InjectorContract.Builder builder) {
        for (BSE<?> n : exports) {
            if (n instanceof ExportedBuildEntry) {
                builder.addProvides(n.getKey());
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void resolve(InjectorResolver resolver) {
        for (ExportedBuildEntry<?> node : exports) {
            if (node.exportedEntry == null) {
                ServiceEntry<?> sn = resolver.internalNodes.getRecursive(node.getKey());
                if (sn == null) {
                    unresolvedExports.computeIfAbsent(node.key(), m -> new HashSet<>()).add(node);
                }
                node.exportedEntry = (ServiceEntry) sn;
                resolvedExports.put(node);
            }
        }
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
        return export0(new ExportedBuildEntry<>(builder, configSite, key));
    }

    public <T> ServiceConfiguration<T> export(ProvidedComponentConfiguration<T> configuration, ConfigSite configSite) {
        if (!(configuration instanceof PackedProvidedComponentConfiguration)) {
            throw new IllegalArgumentException("Custom implementations of " + StringFormatter.format(ProvidedComponentConfiguration.class)
                    + " are not allowed, type = " + StringFormatter.format(configuration.getClass()));
        }
        BSE<T> e = ((PackedProvidedComponentConfiguration<T>) configuration).buildEntry;
        if (e.injectorBuilder != builder) {
            throw new IllegalArgumentException("The specified configuration object was created by another injector extension instance");
        }
        return export0(new ExportedBuildEntry<>(e, configSite));
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
    private <T> ServiceConfiguration<T> export0(ExportedBuildEntry<T> entry) {
        exports.add(entry);
        return new ExportedServiceConfiguration<>(builder.pcc, entry);
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
