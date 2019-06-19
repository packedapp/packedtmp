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

import app.packed.config.ConfigSite;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;

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
    final AbstractComponentConfiguration parent;

    /** The configuration site of the component. */
    private final InternalConfigurationSite site;

    /** Any children this component might have, in order of insertion. */
    final LinkedHashMap<String, AbstractComponentConfiguration> children = new LinkedHashMap<>();

    AbstractComponentConfiguration(InternalConfigurationSite site, AbstractComponentConfiguration parent) {
        this.site = requireNonNull(site);
        this.parent = parent;
    }

    protected void checkConfigurable() {

    }

    public ConfigSite configurationSite() {
        return site;
    }

    void freezeName() {
        // Naming is permanent...
        // When you add a new Component you cannot change the name of previous components.
        // And if a name has not already been set, one is generated (determinisicly).
        //
        // When you call component.name() you cannot set afterwards set the name of the component
        // Maybe have a isGenerated flag, so we can come with some good error text
        //
        // Cannot have a bundle and a component with the same name.
    }

    @Nullable
    public final String getDescription() {
        return description;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public AbstractComponentConfiguration setDescription0(@Nullable String description) {
        checkConfigurable();
        this.description = description;
        return this;
    }

    AbstractComponentConfiguration setName0(@Nullable String name) {
        checkConfigurable();
        checkName(name);
        this.name = name;
        return this;
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

    enum NamingState {
        OPEN, //
        COMPONENT_NAME_CALLED, // which makes it immutable
        FINALIZED // After Wiring and stuff
        ;
    }
}
