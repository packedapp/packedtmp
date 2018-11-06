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

/**
 * A configuration site is the location where an object or parts of it was configured
 */
public interface ConfigurationSite {

    /**
     * 
     * @return
     */
    // Open file
    // Read line
    Optional<ConfigurationSite> getParent();

    /**
     * Returns whether or not this site has a parent.
     * 
     * @return whether or not this site has a parent
     */
    default boolean hasParent() {
        return getParent().isPresent();
    }

    /**
     * Returns the operation that was performed when configuring the object.
     * 
     * @return the operation that was performed when configuring the object
     */
    String operation();
}
