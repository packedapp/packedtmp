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
package packed.internal.inject.service;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import packed.internal.inject.InjectionManager;

/**
 *
 */
public class ServiceManager {

    /** A service exporter handles everything to do with exports of services. */
    @Nullable
    private ServiceExportManager exporter;

    public final InjectionManager im;

    /**
     * @param injectionManager
     */
    public ServiceManager(InjectionManager injectionManager) {
        this.im = requireNonNull(injectionManager);
    }

    public boolean hasExports() {
        return exporter != null;
    }

    /**
     * Returns the {@link ServiceExportManager} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ServiceExportManager exports() {
        ServiceExportManager e = exporter;
        if (e == null) {
            e = exporter = new ServiceExportManager(this);
        }
        return e;
    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    public void resolveExports() {
        if (exporter != null) {
            exporter.resolve();
        }
    }
    // En InjectionManager kan have en service manager...

    // Vi smide alt omkring services der...

    // Lazy laver den...

    // Altsaa det er taenkt tll naar vi skal f.eks. slaa Wirelets op...
    // Saa det der med at resolve. Det er ikke services...
    // men injection...
}
