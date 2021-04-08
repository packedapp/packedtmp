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
package app.packed.component;

import static java.util.Objects.requireNonNull;

/**
 * The base class for component configuration classes.
 * <p>
 * The class is basically a thin wrapper on top of {@link ComponentConfigurationContext}. All component configuration
 * classes must extend, directly or indirectly, from this class.
 * <p>
 * Instead of extending this class directly, you typically want to extend {@link BaseComponentConfiguration} instead.
 */
public abstract class ComponentConfiguration {

    /** The component's configuration context. */
    protected final ComponentConfigurationContext context;

    /**
     * Create a new component configuration.
     * 
     * @param context
     *            the configuration context
     */
    protected ComponentConfiguration(ComponentConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
    }

    /**
     * Ivoked A callback method invoked by Packed immediatly before it is marked as no longer configurable
     */
    protected void onConfigured() {}
}
// I don't expect this class to have any $ methods
// This should most likely be located in the driver instead
// A component configuration is just a thin wrapper
