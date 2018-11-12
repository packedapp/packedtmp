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

import java.util.Map;

import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.inject.ServiceImportFilter;

/**
 *
 */
// Skal nok have 2 versioner, en hvor de er RuntimeNodes og en hvor det er generiske noder, der bliver kopieret.
public class ImportFromInjector implements ServiceImportFilter {

    private final Map<Key<?>, XBuildNodeImportFromInjector<?>> available;

    /** The injector that is being imported from. */
    final Injector injector;

    final Map<Key<?>, XBuildNodeImportFromInjector<?>> m;

    public ImportFromInjector(Injector injector, Map<Key<?>, XBuildNodeImportFromInjector<?>> m) {
        this.injector = requireNonNull(injector);
        this.m = m;
        this.available = Map.copyOf(m);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<Key<?>, ServiceDescriptor> availableServices() {
        return (Map) available;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceConfiguration<T> importService(Key<T> key) {
        XBuildNodeImportFromInjector<?> f = available.get(key);

        // TODO we kal paa en eller anden maade havde sat registranten
        if (f == null) {
            throw new IllegalArgumentException(key + " does not exist, list alternatives....");
        }
        return (ServiceConfiguration<T>) f;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<Key<?>, ServiceConfiguration<?>> importedServices() {
        return (Map) m;
    }
}
