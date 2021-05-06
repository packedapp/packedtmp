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

import app.packed.base.NamespacePath;

/**
 * An base component configuration class that can serve as basis for actual component configuration types. 
 * <p>
 * Component configuration classes do not need to extend this class.
 */
public class BaseComponentConfiguration extends ComponentConfiguration {
   
    /**
     * Creates a new base component configuration.
     * 
     * @param context
     *            the configuration context for the component
     */
    public BaseComponentConfiguration(ComponentConfigurationContext context) {
        super(context);
    }

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #named(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    public NamespacePath path() {
        return context.path();
    }

    /**
     * Sets the {@link Component#name() name} of the component. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the component when the component is initialized, in
     * such a way that it will have a unique name other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see #getName()
     * @see Component#name()
     */
    public BaseComponentConfiguration named(String name) {
        context.named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return context.toString();
    }
}
