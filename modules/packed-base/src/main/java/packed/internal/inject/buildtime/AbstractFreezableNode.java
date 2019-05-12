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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import packed.internal.config.site.InternalConfigurationSite;

/**
 *
 */
public class AbstractFreezableNode {

    /** The configuration site of this object. */
    protected final InternalConfigurationSite configurationSite;

    /** Whether or not the configuration has been frozen. */
    private boolean isFrozen;

    protected AbstractFreezableNode() {
        this.configurationSite = InternalConfigurationSite.UNKNOWN;
    }

    /**
     * Creates a new abstract configuration
     * 
     * @param configurationSite
     *            the configuration site of the configuration
     */
    protected AbstractFreezableNode(InternalConfigurationSite configurationSite) {
        this.configurationSite = requireNonNull(configurationSite);
    }

    protected final void checkConfigurable() {
        if (isFrozen) {
            throw new IllegalStateException("This configuration has been frozen and can no longer be modified");
        }
    }

    /**
     * Returns the configuration site of this configuration.
     * 
     * @return the configuration site of this configuration
     */
    public final InternalConfigurationSite configurationSite() {
        return configurationSite;
    }

    public final void freeze() {
        if (isFrozen) {
            throw new Error();
        }
        isFrozen = true;
        onFreeze();
    }

    protected void onFreeze() {

    }

}
