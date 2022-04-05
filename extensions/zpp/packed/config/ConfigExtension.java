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
package zpp.packed.config;

import app.packed.extension.Extension;

/**
 *
 */

//By default a container does not export any configuration...

// Paa en eller anden maade skal man kunne exportere et skema...
// Er ikke meget for registerSchema(SomeExtension.class, schema)...

public final class ConfigExtension extends Extension<ConfigExtension> {

    // Controls distribution to other extensions, services, ect.

    // Logging, debugging

    // configuration in multiple tempos...
    // For example, for image,

    // Skal vi markere
    //// 3 maader for componenter
    // Deres egen container
    // Almindelige component + Extension.viaExtension(ComponentConfiguration) <- Uses the extensions LifecycleRealm
    // Container/Artifact sidecar

    // Stuff...

    @SuppressWarnings("unchecked")
    void expose(Class<? extends Extension<?>>... extensionTypes) {
        throw new UnsupportedOperationException();
    }

    // structural configuration items

    // Graal?? Class Init vs start...
    void readFrom(String file) { // ConfigurationOption... options???
        // useFileExtension() <-- makes sure FileManager is available at runtime
        // Maybe we can setup a target.... extension.read
    }

    // Will read at initialization time... (using FileExtension)
    // readFrom(String path)
}

// Hvad hvis man egentlig specificere classes, metoder ect. (mht til JPMS)
// Saa skal vi grabbe den der specicere det's security context