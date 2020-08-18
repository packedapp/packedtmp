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

import app.packed.config.ConfigSite;
import app.packed.container.ContainerBundle;
import app.packed.inject.Factory;

/**
 *
 */
public interface ComponentConfiguration {

    /**
     * Checks that the component is still configurable or throws an {@link IllegalStateException}.
     * <p>
     * A component is typically only configurable inside of {@link ContainerBundle#configure()}.
     * 
     * @throws IllegalStateException
     *             if the component is no long configurable.
     */
    void checkConfigurable();

    /**
     * Returns the configuration site that created this configuration.
     * 
     * @return the configuration site that created this configuration
     */
    ConfigSite configSite();

    /**
     * Returns the name of the component. If no name has previously been set via {@link #setName(String)} a name is
     * automatically generated by the runtime as outlined in {@link #setName(String)}.
     * <p>
     * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
     * being thrown.
     * 
     * @return the name of the component
     * @see #setName(String)
     */
    String getName();

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #setName(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    ComponentPath path(); // systemPath()<--- // artifactPath()???

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
    ComponentConfiguration setName(String name);

    /**
     * Creates a new container with this container as its parent by linking the specified bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            any wirelets
     */
    void link(Bundle<?> bundle, Wirelet... wirelets);

    /**
     * Wires a new child component using the specified driver
     * 
     * @param <C>
     *            the type of configuration returned by the driver
     * @param driver
     *            the driver to use for creating the component
     * @param wirelets
     *            any wirelets that should be used when creating the component
     * @return a configuration for the component
     */
    <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets);

    <C, I> C wire(ClassSourcedDriver<C, I> driver, Class<I> implementation, Wirelet... wirelets);

    <C, I> C wire(FactorySourcedDriver<C, I> driver, Factory<I> implementation, Wirelet... wirelets);

    <C, I> C wireInstance(InstanceSourcedDriver<C, I> driver, I instance, Wirelet... wirelets);
}

///**
//* If this component has been installed from an extension, returns the extension. Otherwise returns empty.
//* 
//* @return any extension this component belongs to
//*/
//// Syntes vi flytter den til en attribute....
//// Og det samme med description??
//// BaseExtensionAttributes
////
//Optional<Class<? extends Extension>> extension();

///**
//* Returns the class that defines the container.
//* 
//* @return the class that defines the container
//*/
// Is replaced with someking of compoentn descriptor
//Class<?> sourceType();
