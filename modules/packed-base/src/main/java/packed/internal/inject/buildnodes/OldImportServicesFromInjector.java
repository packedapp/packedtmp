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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * 
 */
// Skal nok have 2 versioner, en hvor de er RuntimeNodes og en hvor det er generiske noder, der bliver kopieret.
public class OldImportServicesFromInjector implements ServiceStagingArea {

    /** The configuration site where the injector was imported. */
    final InternalConfigurationSite configurationSite;

    /** */
    final Map<Key<?>, ServiceDescriptor> exposed;

    /** A map of all services that have been imported. */
    final Map<Key<?>, BuildNodeImportServiceFromInjector<?>> importedServices = new HashMap<>();

    /** The injector that is being imported from. */
    final Injector injector;

    /** The configuration of the injector. */
    final InternalInjectorConfiguration injectorConfiguration;

    final boolean autoImport;

    public OldImportServicesFromInjector(InternalInjectorConfiguration injectorConfiguration, Injector injector, InternalConfigurationSite configurationSite,
            boolean autoImport) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.injector = requireNonNull(injector);
        this.configurationSite = requireNonNull(configurationSite);
        this.exposed = Map.copyOf(injector.services().collect(Collectors.toMap(e -> e.getKey(), e -> e)));
        this.autoImport = autoImport;
    }

    /** {@inheritDoc} */
    @Override
    public void export(Key<?> key) {
        throw new UnsupportedOperationException("Export is not supported when importing an already instantiated Injector");
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> exportedServices() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    public Map<Key<?>, ServiceDescriptor> exposedServices() {
        return exposed;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceStagingArea importAllServices() {
        if (autoImport) {
            exposedServices().keySet().forEach(key -> importServicex(key, configurationSite));
        } else {
            importAllServices(e -> true);
        }
        return this;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<Key<?>, ServiceConfiguration<?>> importedServices() {
        return (Map) Collections.unmodifiableMap(importedServices);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> ServiceConfiguration<T> importService(Key<T> key) {
        BuildNodeImportServiceFromInjector<T> f = (BuildNodeImportServiceFromInjector<T>) importedServices.get(key);
        if (f != null) {
            return f;
        }
        ServiceDescriptor descriptor = exposed.get(key);
        if (descriptor != null) {
            InternalConfigurationSite ics = (InternalConfigurationSite) descriptor.getConfigurationSite();
            InternalConfigurationSite cs = ics.spawnStack(ConfigurationSiteType.INJECTOR_IMPORT_SERVICE);
            BuildNodeImportServiceFromInjector<T> bn = new BuildNodeImportServiceFromInjector<>(this, cs, descriptor);
            bn.as((Key) descriptor.getKey());
            bn.setDescription(descriptor.getDescription());
            bn.tags().addAll(descriptor.tags());
            importedServices.put(key, bn);
            return bn;
        } else {
            throw new IllegalArgumentException(key + " does not exist, list alternatives....");
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> ServiceConfiguration<T> importServicex(Key<T> key, InternalConfigurationSite ics) {
        BuildNodeImportServiceFromInjector<T> f = (BuildNodeImportServiceFromInjector<T>) importedServices.get(key);
        if (f != null) {
            return f;
        }
        ServiceDescriptor descriptor = exposed.get(key);
        if (descriptor != null) {
            InternalConfigurationSite cs = ics.replaceParent(descriptor.getConfigurationSite());
            BuildNodeImportServiceFromInjector<T> bn = new BuildNodeImportServiceFromInjector<>(this, cs, descriptor);
            bn.as((Key) descriptor.getKey());
            bn.setDescription(descriptor.getDescription());
            bn.tags().addAll(descriptor.tags());
            importedServices.put(key, bn);
            return bn;
        } else {
            throw new IllegalArgumentException(key + " does not exist, list alternatives....");
        }
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
}
