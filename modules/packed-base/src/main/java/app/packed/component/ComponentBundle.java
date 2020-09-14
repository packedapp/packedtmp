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

import app.packed.base.TreePath;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;

/** A bundle that uses a ComponentConfiguration as the underlying configuration object. */
public abstract class ComponentBundle<T extends ComponentConfiguration> extends Bundle<T> {

    /**
     * Creates a new bundle using the supplied driver.
     * 
     * @param driver
     *            the driver to use for constructing the bundles configuration object
     */
    protected ComponentBundle(ComponentDriver<? extends T> driver) {
        super(driver);
    }

//    /**
//     * @param <X>
//     *            the type of instance
//     * @param driver
//     * @param instance
//     *            the instance to wrap
//     */
//    protected <X> ComponentBundle(InstanceSourcedDriver<? extends T, X> driver, X instance) {
//        super(driver.bindToInstance(instance));
//    }

    /**
     * Checks that the {@link #configure()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does not try to configure the extension after it has been configured.
     * 
     * @throws IllegalStateException
     *             if {@link #configure()} has been invoked
     * @see ContainerConfiguration#checkConfigurable()
     */
    protected final void checkConfigurable() {
        configuration().checkConfigurable();
    }

    /**
     * Returns the configuration site of this bundle.
     * 
     * @return the configuration site of this bundle
     * @see ContainerConfiguration#configSite()
     */
    protected final ConfigSite configSite() {
        return configuration().configSite();
    }

    /**
     * Returns the name of the container. If no name has previously been set via {@link #setName(String)} a name is
     * automatically generated by the runtime as outlined in {@link #setName(String)}.
     * <p>
     * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
     * being thrown.
     * 
     * @return the name of the container
     * @see #setName(String)
     * @see ContainerConfiguration#setName(String)
     */
    protected final String getName() {
        return configuration().getName();
    }

    /**
     * Links the specified bundle as a child to this bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            an optional array of wirelets
     * @see ContainerConfiguration#link(Bundle, Wirelet...)
     */
    protected final void link(Bundle<?> bundle, Wirelet... wirelets) {
        configuration().link(bundle, wirelets);
    }

    /**
     * Returns the full path of the container that this bundle creates.
     * 
     * @return the full path of the container that this bundle creates
     * @see ContainerConfiguration#path()
     */
    protected final TreePath path() {
        return configuration().path();
    }

    /**
     * Sets the name of the container. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * This method should be called as the first thing when configuring a container.
     * <p>
     * If no name is set using this method. A name will be assigned to the container when the container is initialized, in
     * such a way that it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the container
     * @see #getName()
     * @see ContainerConfiguration#setName(String)
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @throws IllegalStateException
     *             if called from outside {@link #configure()}
     */
    protected final void setName(String name) {
        configuration().setName(name);
    }

    protected final <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return configuration().wire(driver, wirelets);
    }

    protected final <C, I> C wire(ClassComponentDriver<C, I> driver, Class<? extends I> implementation, Wirelet... wirelets) {
        return configuration().wire(driver, implementation, wirelets);
    }

    protected final <C, I> C wire(FactoryComponentDriver<C, I> driver, Factory<I> factory, Wirelet... wirelets) {
        return configuration().wire(driver, factory, wirelets);
    }

    protected final <C, I> C wireInstance(InstanceComponentDriver<C, I> driver, I instance, Wirelet... wirelets) {
        return configuration().wireInstance(driver, instance, wirelets);
    }
}
