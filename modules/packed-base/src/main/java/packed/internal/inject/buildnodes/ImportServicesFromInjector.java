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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.inject.ServiceStagingArea;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * 
 */
// Skal nok have 2 versioner, en hvor de er RuntimeNodes og en hvor det er generiske noder, der bliver kopieret.
public class ImportServicesFromInjector implements ServiceStagingArea {

    /** A map of all services that have been imported. */
    final Map<Key<?>, BuildNodeImportServiceFromInjector<?>> imports = new HashMap<>();

    /** The injector that is being imported from. */
    final Injector injector;

    final Map<Key<?>, ServiceDescriptor> exposed;

    final InternalConfigurationSite configurationSite;
    final InternalInjectorConfiguration injectorConfiguration;

    public ImportServicesFromInjector(InternalInjectorConfiguration injectorConfiguration, Injector injector, InternalConfigurationSite configurationSite) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.injector = requireNonNull(injector);
        this.configurationSite = requireNonNull(configurationSite);
        this.exposed = Map.copyOf(injector.services().collect(Collectors.toMap(e -> e.getKey(), e -> e)));
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> ServiceConfiguration<T> importService(Key<T> key) {
        BuildNodeImportServiceFromInjector<T> f = (BuildNodeImportServiceFromInjector<T>) imports.get(key);
        if (f != null) {
            return f;
        }
        ServiceDescriptor descriptor = exposed.get(key);
        if (descriptor != null) {
            InternalConfigurationSite cs = configurationSite.spawnStack(ConfigurationSiteType.INJECTOR_IMPORT_SERVICE);
            BuildNodeImportServiceFromInjector<T> bn = new BuildNodeImportServiceFromInjector<>(this, cs, descriptor);
            bn.as((Key) descriptor.getKey());
            bn.setDescription(descriptor.getDescription());
            bn.tags().addAll(descriptor.tags());
            imports.put(key, bn);
            return bn;
        } else {
            throw new IllegalArgumentException(key + " does not exist, list alternatives....");
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<Key<?>, ServiceConfiguration<?>> importedServices() {
        return (Map) imports;// Make immutable??? Or at least semi immutable
    }

    /** {@inheritDoc} */
    @Override
    public Map<Key<?>, ServiceDescriptor> exposedServices() {
        return exposed;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> optionalServices() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> requiredServices() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    public void export(Key<?> key) {
        throw new UnsupportedOperationException("Export is not supported when importing an already instantiated Injector");
    }
}
