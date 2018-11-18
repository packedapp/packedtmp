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
package app.packed.inject;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 
 */

// Vi splitter dem ud i klasser, fordi vi bliver noedt til at separere import og export i 2 klasser.
// For at man kan ServiceExport til et modul. Bliver man noedt til at importere.
// alle services fra alle bundles der ligger i samme lag.

// InjectorImporter? Saa kan vi have ContainerImporter, og behoever ikke en million klasser...

// Man skal kunne kombinere dem som man har lyst, de bliver koert i raekkefoelge.
// Hvis eksempel, SIF.SYSTEM_OUT, addTags()

// If hasQualifier(Red.class) register withQualifier(Blue.class)

// All services are passed through one filter, before passed on to the next filter...

// Or InjectorImportFilter
public class ServiceImportFilter extends ServiceFilter {

    // @Inject not supported....

    public static final ServiceImportFilter ALL = new ServiceImportFilter();// Why???

    /** A filter that rejects all services. */
    public static final ServiceImportFilter NONE = new ServiceImportFilter();

    /** Show test example, with complex filtering first, and then Systemout */
    public static final ServiceImportFilter SYSTEM_OUT = new ServiceImportFilter();

    /** Creates a new filter */
    protected ServiceImportFilter() {}

    /**
     * Creates a new filter
     * 
     * @param lookup
     *            a lookup object that will be used for invoking methods annotated with {@link Provides}.
     */
    protected ServiceImportFilter(MethodHandles.Lookup lookup) {
        super(lookup);
    }

    protected final ServiceConfiguration<?> clone(ServiceConfiguration<?> sc) {
        return sc;// Do we ever need to make a service available under two different keys
    }

    @Provides
    public String convert(@Left String xx) {
        return xx.toUpperCase();
    }

    /**
     * Returns a map of all service
     * 
     * @return stuff
     */
    protected final Map<Key<?>, ServiceDescriptor> exposedServices() {
        throw new UnsupportedOperationException();
    }

    protected final Map<Key<?>, ServiceConfiguration<?>> imoortedServices() {
        throw new UnsupportedOperationException();
    }

    protected void filter(ServiceConfiguration<?> configuration) {
        configuration.asNone();
    }

    // Mange maader man kan goere det paa. F.eks. asNone.
    // Predicates, ect.

    // Men 1.
    // Man skal kunne sprede ting.
    // For eksempel. Skal vi kunne tage et PropertyMap
    // Og split det ud i Key<@XXX String>.
    // Det vil sige en service til mange...

    // Returns an import filter that accepts all the services selected by the specified predicate

    /**
     * Creates
     * 
     * @param originalKey
     *            the key of the original service
     * @param newKey
     *            the key that the service should be rebound to
     * @return the new filter
     */
    public static ServiceImportFilter rebind(Key<?> originalKey, Key<?> newKey) {

        throw new UnsupportedOperationException();
    }

    public static ServiceImportFilter accept(Predicate<? super ServiceDescriptor> predicate) {
        throw new UnsupportedOperationException();
    }

    public static ServiceImportFilter reject(Predicate<? super ServiceDescriptor> predicate) {
        throw new UnsupportedOperationException();
    }

    @Provides
    public static String transform(String ss) {
        return ss.toUpperCase();
    }

    // Eksempel med logger
}

// Kan kun angive en import