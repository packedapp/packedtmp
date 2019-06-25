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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Optional;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.DefaultContainerConfiguration.NameWirelet;

/** An abstract base class for the configuration of a component. */
abstract class AbstractComponentConfiguration {

    /** The description of the component. */
    @Nullable
    String description;

    /** The name of the component. */
    @Nullable
    String name;

    /** Any parent that the component has. */
    @Nullable
    final DefaultContainerConfiguration parent;

    /** The configuration site of the component. */
    private final InternalConfigurationSite site;

    /** Any children this component might have, in order of insertion. */
    LinkedHashMap<String, AbstractComponentConfiguration> children;

    AbstractComponentConfiguration(InternalConfigurationSite site, DefaultContainerConfiguration parent) {
        this.site = requireNonNull(site);
        this.parent = parent;
    }

    protected void checkConfigurable() {

    }

    public ConfigSite configurationSite() {
        return site;
    }

    @Nullable
    public final String getDescription() {
        return description;
    }

    public final String getName() {
        String n = name;
        if (n == null) {
            lazyInitializeName(NamingState.GET_NAME_CALLED, null);
        }
        return name;
    }

    public AbstractComponentConfiguration setDescription0(String description) {
        // ContainerConfiguration does not currently extend ComponentConfiguration
        requireNonNull(description, "description is null");
        checkConfigurable();
        this.description = description;
        return this;
    }

    enum E {
        BY_WIRE, BY_SET, IMPLICIT
    }

    protected void lazyInitializeName(NamingState reason, String name) {
        if (this.name != null) {
            return;
        }
        E e = E.IMPLICIT;
        if (name != null) {
            e = E.BY_SET;
        }
        String n = name;
        if (this instanceof DefaultContainerConfiguration) {
            Optional<NameWirelet> o = ((DefaultContainerConfiguration) this).wirelets().last(NameWirelet.class);
            if (o.isPresent()) {
                n = o.get().name;
                e = E.BY_WIRE;
            }
        }
        if (n == null) {
            if (parent == null) {
                this.name = "App";
                return; // TODO fix
            }
            if (this instanceof DefaultContainerConfiguration) {
                n = ((DefaultContainerConfiguration) this).ccc.defaultPrefix();
            } else {
                n = ((DefaultComponentConfiguration) this).ccd.defaultPrefix();
            }
        }

        lazyInitializeName(e, n);
        this.namingState = reason;
        //
        // if (namingState == NamingState.MUTABLE) {
        // // Finalize name
        // // See if we have any wirelet that overrides the name (wirelet name has already been verified)
        //
        // // TODO make name unmodifiable
        // if (name == null) {
        // if (parent == null) {
        // name = "App";
        // } else {
        // name = finalizeNameWithPrefix(ccc.defaultPrefix());
        // }
        // } else {
        // String n = name;
        // if (n.endsWith("?")) {
        // name = finalizeNameWithPrefix(n.substring(0, n.length() - 1));
        // } else if (parent != null && parent.children != null && parent.children.containsKey(n)) {
        // if (parent.children.get(name) != this) {
        // throw new IllegalStateException();
        // }
        // }
        // }

        // }
    }

    private void lazyInitializeName(E e, String name) {
        if (!name.endsWith("?")) {
            if (parent == null || parent.children == null || !parent.children.containsKey(name)) {
                this.name = name;
                return;
            }
            throw new RuntimeException("Name already exist " + name);
        }

        String prefix = name.substring(0, name.length() - 1);
        String newName = prefix;
        int counter = 0;
        for (;;) {
            if (parent.children == null || !parent.children.containsKey(newName)) {
                name = newName;
                return;
            }
            // Maybe now keep track of the counter... In a prefix hashmap, Its probably benchmarking code though
            // But it could also be a host???
            newName = prefix + counter++;
        }
    }

    AbstractComponentConfiguration setName0(String name) {
        requireNonNull(name, "name is null");
        checkName(name);
        checkConfigurable();
        if (namingState == NamingState.MUTABLE) {
            lazyInitializeName(NamingState.SET_NAME_CALLED, name);
            // We only update this.name if wiring sets a name, never naming state
            // So make sure we do it here
            this.namingState = NamingState.SET_NAME_CALLED;
            return this;
        } else if (namingState == NamingState.SET_NAME_CALLED) {
            throw new IllegalStateException("setName has already been invoked once");
        } else if (namingState == NamingState.LINK_CALLED) {
            throw new IllegalStateException("Cannot call this method after containerConfiguration.link has been invoked");
        } else if (namingState == NamingState.NEW_COMPONENT_CALLED) {
            // How this work @Install????
            // Maybe we can have a installFromScan method(), that is implicit called from the end of the configure method
            throw new IllegalStateException("Cannot call this method after installing new components in the container");
        } else /* if (namingState == NamingState.GET_NAME_CALLED) */ {
            throw new IllegalStateException("Cannot call this method after calling getName()");
        }
    }

    /**
     * Checks the name of the component.
     * 
     * @param name
     *            the name to check
     * @return the name if valid
     */
    private static String checkName(String name) {
        if (name != null) {

        }
        return name;
    }

    NamingState namingState = NamingState.MUTABLE;

    enum NamingState {
        /** */
        MUTABLE,

        /** Used has called {@link ComponentConfiguration#getName()}. */
        GET_NAME_CALLED, SET_NAME_CALLED,

        /** */
        LINK_CALLED,
        /** */
        NEW_COMPONENT_CALLED
        /* FINALIZED, we just check configurable */

        ;
    }
}
