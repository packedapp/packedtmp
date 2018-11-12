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
package app.packed.util;

import java.util.Optional;

import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A configuration site is the location where a object was configured. This is typically either a combination of method
 * and a line number, or a filename and a line number. A configuration site can have are parent, for example, the parent
 * of registration of a service will the the registration point for the injector in which the service is registered in.
 * 
 */
// ConfigurationLocation????
/**
 *
 */
public interface ConfigurationSite {

    /** A site that is used if a location of configuration site could not be determined. */
    ConfigurationSite UNKNOWN = InternalConfigurationSite.UNKNOWN;

    /**
     * Returns whether or not this site has a parent.
     * 
     * @return whether or not this site has a parent
     */
    default boolean hasParent() {
        return parent().isPresent();
    }

    /**
     * Returns the configuration operation that was performed.
     * 
     * @return the configuration operation that was performed
     */
    String operation();

    /**
     * Returns any parent this site may have, or an empty {@link Optional} if this site has no parent.
     * 
     * @return any parent this site may have, or an empty {@link Optional} if this site has no parent
     */
    Optional<ConfigurationSite> parent();
}
// Example with Provides
// The exist because the "inject.provides" because of field xxxxx
// This annotation was scanned, because an object was registered at this point
// It was registered in the container xyz
