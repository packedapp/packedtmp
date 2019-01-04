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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A configuration site represents the location where an object was configured/registered. This is typically either a
 * combination of a method and a line number, or a filename and a line number.
 * <p>
 * A configuration site can have a parent, for example, the parent of a service registration will be the registration
 * point of its injector.
 */
public interface ConfigurationSite {

    /** A special configuration site that is used if the actual configuration site could not be determined. */
    ConfigurationSite UNKNOWN = InternalConfigurationSite.UNKNOWN;

    // ConfigurationSite STACK_TRACE_MISSING

    /**
     * Performs the given action on each element in configuration site chain, traversing from the top configuration site.
     *
     * @param action
     *            an action to be performed on each {@code ConfigurationSite} in the chain
     */
    default void forEach(Consumer<? super ConfigurationSite> action) {
        requireNonNull(action, "action is null");
        ConfigurationSite cs = this;
        while (cs != null) {
            action.accept(cs);
            cs = cs.parent().orElse(null);
        }
    }

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
    // If open up for custom operations... We should probably have a naming scheme...
    // maybe prefix all with packed.injectorBind
    String operation();

    /**
     * Returns any parent this site may have, or an empty {@link Optional} if this site has no parent.
     * 
     * @return any parent this site may have, or an empty {@link Optional} if this site has no parent
     */
    Optional<ConfigurationSite> parent();

    default void print() {
        forEach(e -> System.out.println(e));
    }
    // visit(ConfigurationSiteVisitor visitor)
}
// Example with Provides
// The exist because the "inject.provides" because of field xxxxx
// This annotation was scanned, because an object was registered at this point
// It was registered in the container xyz
