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
import app.packed.bean.BeanConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;

/**
 * The base class for all component configuration classes.
 * <p>
 * This class can only be extended through one of its subclasses {@link BeanConfiguration} or
 * {@link ContainerConfiguration}.
 */
public abstract sealed class ComponentConfiguration permits BeanConfiguration, ContainerConfiguration {

    protected abstract void checkIsConfigurable();
    
    protected abstract void checkIsCurrent();

    /**
     * Sets the name of the component. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * If no name is explicitly set on a component. A name will be assigned to the component (at build time) in such a way
     * that it will have a unique name among other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see ComponentMirror#name()
     * @see Wirelet#named(String)
     */
    public abstract ComponentConfiguration named(String name);

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
    public abstract NamespacePath path();
}
