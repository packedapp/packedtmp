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

import app.packed.config.ConfigSite;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;

/**
 *
 */
abstract class AbstractNamedConfiguration {

    /** The description of the entity. */
    @Nullable
    String description;

    /** The name of the entity. */
    @Nullable
    String name;

    @Nullable
    final DefaultContainerConfiguration parent;

    /** The configuration site of the component. */
    public final InternalConfigurationSite site;

    AbstractNamedConfiguration(InternalConfigurationSite site, DefaultContainerConfiguration parent) {
        this.site = requireNonNull(site);
        this.parent = parent;
    }

    protected abstract void checkConfigurable();

    public ConfigSite configurationSite() {
        return site;
    }

    @Nullable
    public final String getDescription() {
        return description;
    }

    @Nullable
    public String getName() {
        return name;
    }

    void freezeName() {

    }

    /**
     * Checks the name of the container or component.
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

    public AbstractNamedConfiguration setDescription(@Nullable String description) {
        checkConfigurable();
        this.description = description;
        return this;
    }

    AbstractNamedConfiguration setName0(@Nullable String name) {
        checkConfigurable();
        checkName(name);
        this.name = name;
        return this;
    }
}
