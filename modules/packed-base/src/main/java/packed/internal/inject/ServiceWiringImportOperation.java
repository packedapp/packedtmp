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
package packed.internal.inject;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import app.packed.bundle.BundleLink;
import app.packed.bundle.Wirelet;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.buildtime.BuildtimeServiceNode;

/**
 * A stage that is executed during the import phase of an injector or module. A typically usage is to restrict what
 * services are imported from an injector or bundle.
 * <p>
 * In most cases the functionality provided by the various static methods will be enough.
 * 
 * 
 */
public abstract class ServiceWiringImportOperation extends Wirelet {

    /**
     * Processes each service.
     * 
     * @param sc
     *            the service configuration
     */
    // IDeen er lidt at kalde alle der procerere mere end en entity onEachX, og resten onX
    public void onEachService(BuildtimeServiceNode<?> sc) {}

    @Override
    protected void process(BundleLink link) {
        throw new UnsupportedOperationException();
    }
}

class XImportVer2 {

    protected final ServiceConfiguration<?> clone(ServiceConfiguration<?> sc) {
        return sc;// Do we ever need to make a service available under two different keys
    }

    // Det er jo ikke noget man importere.....at nogle der flowe mellem bundlen..
    protected String injectorDescription(@Nullable String injectorDescription) {
        return injectorDescription;
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

    public static <T, S> Wirelet adapt(Key<T> key1, Key<T> newKey1, Function<S, T> f) {
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