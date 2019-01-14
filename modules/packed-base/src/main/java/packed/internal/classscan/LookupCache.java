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
package packed.internal.classscan;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import packed.internal.container.LookupValue;
import packed.internal.invokers.ExecutableInvoker;
import packed.internal.invokers.InternalFunction;

/**
 * A cache for lookup objects
 */
public final class LookupCache {

    /** A cache of service class descriptors. */
    private static final LookupValue<LookupCache> LOOKUP_CACHE = new LookupValue<>() {

        @Override
        protected LookupCache computeValue(Lookup lookup) {
            return new LookupCache(lookup);
        }
    };

    /** The default public accessor object. */
    public static final LookupCache PUBLIC = get(MethodHandles.publicLookup());

    /** A cache of service class descriptors. */
    private final ClassValue<ComponentClassDescriptor> componentClassCache = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ComponentClassDescriptor computeValue(Class<?> type) {
            return new ComponentClassDescriptor(type, lookup, MemberScanner.forComponent(type, lookup));
        }
    };

    /** A cache of service class descriptors. */
    private final ClassValue<ImportExportDescriptor> importExportStageClassCache = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ImportExportDescriptor computeValue(Class<?> type) {
            return new ImportExportDescriptor(type, lookup, MemberScanner.forImportExportStage(type, lookup));
        }
    };

    /** The lookup object. */
    private final MethodHandles.Lookup lookup;

    /** A cache of service class descriptors. */
    private final ClassValue<ServiceClassDescriptor> serviceClassCache = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ServiceClassDescriptor computeValue(Class<?> type) {
            return new ServiceClassDescriptor(type, lookup, MemberScanner.forService(type, lookup));
        }
    };

    private LookupCache(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup);
    }

    public <T> ComponentClassDescriptor getComponentDescriptor(Class<T> implementation) {
        return componentClassCache.get(requireNonNull(implementation, "implementation is null"));
    }

    public <T> ImportExportDescriptor getImportExportStage(Class<T> implementation) {
        return importExportStageClassCache.get(requireNonNull(implementation, "implementation is null"));
    }

    /**
     * Returns a service class descriptor for the specified implementation type.
     * 
     * @param <T>
     *            the type of the descriptor
     * @param implementation
     *            the implementation type to return a descriptor for
     * @return a service descriptor for the specified implementation type
     */
    public ServiceClassDescriptor getServiceDescriptor(Class<?> implementation) {
        return serviceClassCache.get(requireNonNull(implementation, "implementation is null"));
    }

    public <T> InternalFunction<T> readable(InternalFunction<T> factory) {
        // TODO add field...
        if (factory instanceof ExecutableInvoker) {
            ExecutableInvoker<T> e = (ExecutableInvoker<T>) factory;
            if (!e.hasMethodHandle()) {
                return e.withLookup(lookup);
            }
        }
        return factory;
    }
    // install as component class
    // install as component instance
    // install as mixin class
    // install as mixin instance
    // install as service class
    // install as service instance
    // newInstance()

    public static LookupCache get(MethodHandles.Lookup lookup) {
        return LOOKUP_CACHE.get(lookup);
    }

    // Naar vi laver factoriet ved vi ikke hvordan det skal bruges....
    // Det er heller ikke fordi vi kender noget til Lookup objektet...

    // Saa vi skal have en special struktur til det. Der er uafhaendig af hele invokerings cirkuset....

    // InternalFactory, kan heller ikke vide noget om det, med mindre vi laver en withLookup();
}
