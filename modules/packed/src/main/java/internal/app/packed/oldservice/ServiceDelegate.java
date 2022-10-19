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
package internal.app.packed.oldservice;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;

import internal.app.packed.oldservice.build.ServiceSetup;

/**
 *
 */
// En wrapper der goer at vi kan delay lidt det at smide exceptiosn for dublicate keys.
public class ServiceDelegate {

    private ServiceSetup service;

    public ServiceSetup getSingle() {
        requireNonNull(service);
        return service;
    }

    void resolve(InternalServiceExtension sbm, ServiceSetup b) {
        if (service != null) {
            LinkedHashSet<ServiceSetup> hs = sbm.errorManager().failingDuplicateProviders.computeIfAbsent(b.key(), m -> new LinkedHashSet<>());
            hs.add(b); // might be added multiple times, hence we use a Set, but add existing first
            hs.add(service);
        } else {
            this.service = b;
        }
    }
}
