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
package app.packed.bundle;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import app.packed.bundle.BundlingImportStage;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;

/**
 * A stage that is executed during the import phase of an injector or module. A typically usage is to restrict what
 * services are imported from an injector or bundle.
 * <p>
 * In most cases the functionality provided by the various static methods will be enough.
 * 
 * 
 */
public abstract class BundlingImportStage extends BundlingStage {

    /**
     * An import stage that imports no services by invoking {@link ServiceConfiguration#asNone()} on every processed
     * configuration.
     */
    // Rename to noImports() method -> will be much easier to make stages mutable if needed
    // Skal vi have noget med services med?????
    public static final BundlingImportStage NO_SERVICES = new BundlingImportStage() {

        @Override
        public void onEachService(ServiceConfiguration<?> sc) {
            sc.asNone();
        }
    };

    /** Creates a new stage */
    protected BundlingImportStage() {}

    /**
     * Creates a new stage with a lookup object. This constructor is only needed if the extending class makes use of the
     * {@link Provides} annotation.
     * 
     * @param lookup
     *            a lookup object that will be used for invoking methods annotated with {@link Provides}.
     */
    protected BundlingImportStage(MethodHandles.Lookup lookup) {
        super(lookup);
    }

    protected String injectorDescription(@Nullable String injectorDescription) {
        return injectorDescription;
    }

    /**
     * Processes each service.
     * 
     * @param sc
     *            the service configuration
     */
    @Override
    // IDeen er lidt at kalde alle der procerere mere end en entity onEachX, og resten onX
    protected void onEachService(ServiceConfiguration<?> sc) {}
}

class XImportVer2 {

    protected final ServiceConfiguration<?> clone(ServiceConfiguration<?> sc) {
        return sc;// Do we ever need to make a service available under two different keys
    }

    /**
     * Returns a map of all service
     * 
     * @return stuff
     */
    // Don't know if we want this method?????, or we are strictly a passthrough biatch
    protected final Map<Key<?>, ServiceDescriptor> exposedServices() {
        throw new UnsupportedOperationException();
    }

    protected final Map<Key<?>, ServiceConfiguration<?>> imoortedServices() {
        throw new UnsupportedOperationException();
    }

    public static <T, S> BundlingImportStage adapt(Key<T> key1, Key<T> newKey1, Function<S, T> f) {
        // Nahhh kun supporte provides her.. Evt. Factory...
        throw new UnsupportedOperationException();

        // Hvis eksempel med Vector

    }

    // Men 1.
    // Man skal kunne sprede ting.
    // For eksempel. Skal vi kunne tage et PropertyMap
    // Og split det ud i Key<@XXX String>.
    // Det vil sige en service til mange...
    // Ikke supporteret

    static class OldService {
        String importInfo;
        String nonimportantInfo;
        Date startdato;
        Date stopdato;
        TimeZone timeZone;
    }
}