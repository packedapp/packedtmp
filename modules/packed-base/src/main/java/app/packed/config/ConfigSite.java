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
package app.packed.config;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import packed.internal.config.site.InternalConfigSite;

/**
 * A configuration site represents the location where an object was configured/registered. This can, for example, be a
 * filename and a line number, an annotated method, or a class file with a line number.
 * <p>
 * A configuration site can have a parent, thereby nesting for example, tthe parent of a service registration will be
 * the registration point of its injector.
 */
// We need to open up... If this a generic mechanism...

// Can we intern them????? ClassValue<ConfigSite>
// 99% of the time they will probably have the same parents...
// Maybe store a hash... for the total configuration site.
// No matter what, we should never new ConfigSite*** in any way

// Can lazily generate line numbers from AnnotatedMethods+fields via reading of classinfo

// https://api.flutter.dev/flutter/package-source_span_source_span/package-source_span_source_span-library.html

// ConfigSite chain
// https://api.flutter.dev/flutter/package-stack_trace_stack_trace/Chain-class.html
public interface ConfigSite {

    /** A special configuration site that is used if the actual configuration site could not be determined. */
    ConfigSite UNKNOWN = InternalConfigSite.UNKNOWN;

    /**
     * Performs the given action on each element in configuration site chain, traversing from the top configuration site.
     *
     * @param action
     *            an action to be performed on each {@code ConfigSite} in the chain
     */
    default void forEach(Consumer<? super ConfigSite> action) {
        requireNonNull(action, "action is null");
        var cs = this;
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
     * Returns the name of the configuration operation that was performed.
     * 
     * @return the name of the configuration operation that was performed
     */
    // If open up for custom operations... We should probably have a naming scheme...
    // maybe prefix all with packed.injectorBind
    String operation();

    /**
     * Returns any parent this site may have, or an empty {@link Optional} if this site has no parent.
     * 
     * @return any parent this site may have, or an empty {@link Optional} if this site has no parent
     */
    // Rename to cause????
    Optional<ConfigSite> parent();

    default void print() {
        forEach(e -> System.out.println(e));
    }

    /**
     * Visits this configuration site. {@link #visitEach(ConfigSiteVisitor)} will also visit each descendant of this
     * configuration site;
     * 
     * @param visitor
     *            the visitor
     */
    void visit(ConfigSiteVisitor visitor);

    default void visitEach(ConfigSiteVisitor visitor) {
        forEach(s -> s.visit(visitor));
    }
}

// Example with Provides
// The exist because the "inject.provides" because of field xxxxx
// This annotation was scanned, because an object was registered at this point
// It was registered in the container xyz

// Actions that returns new configuration site by modifying the old ones.
// replace parent...
// splice.
// withOperation -> Changes the operation
// Many things we can do
