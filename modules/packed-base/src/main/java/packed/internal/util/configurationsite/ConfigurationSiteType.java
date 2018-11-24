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
package packed.internal.util.configurationsite;

import static java.util.Objects.requireNonNull;

/**
 *
 */

// Separate for bundle/configuration?????
public enum ConfigurationSiteType {

    /** */
    BUNDLE_DESCRIPTOR_OF("Descriptor.of"),

    /** */
    BUNDLE_EXPOSE("Injector.expose"),

    /** */
    INJECTOR_CONFIGURATION_BIND("Injector.bind"),

    /** */
    INJECTOR_CONFIGURATION_INJECTOR_BIND("Injector.injectorBind"),

    /** */
    INJECTOR_OF("Injector.of"),

    /** */
    INJECTOR_PROVIDE("Injector.provide");

    final String f;

    ConfigurationSiteType(String f) {
        this.f = requireNonNull(f);
    }

    public String operation() {
        return f;
    }

    @Override
    public String toString() {
        return f;
    }
}
