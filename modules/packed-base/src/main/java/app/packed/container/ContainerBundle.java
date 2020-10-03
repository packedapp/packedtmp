/*
  * Copyright (c) 2008 Kasper Nielsen.
? *
?
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
package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.base.TreePath;
import app.packed.component.BeanConfiguration;
import app.packed.component.Bundle;
import app.packed.component.ComponentClassDriver;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentFactoryDriver;
import app.packed.component.ComponentInstanceDriver;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;

/**
 * Bundles are the main source of configuration for containers and artifacts. Basically a bundle is just a thin wrapper
 * around {@link ContainerConfiguration}. Delegating every invocation in the class to an instance of
 * {@link ContainerConfiguration} available via {@link #configuration()}.
 * <p>
 * A bundle instance can be used ({@link #configure()}) exactly once. Attempting to use it multiple times will fail with
 * an {@link IllegalStateException}.
 * 
 * A generic bundle. Normally you would extend {@link BaseBundle}
 */

// Nej der er ingen grund til at lave den concurrent. Som regel er det en ny instans...

// Maybe introduce ContainerBundle()... Det jeg taenker er at introduce noget der f.eks. kan bruges i kotlin
// saa man kan noget der minder om https://ktor.io
// Altsaa en helt barebones bundle

// Kunne godt have nogle lifecycle metoder man kunne overskrive.
// F.eks. at man vil validere noget

public abstract class ContainerBundle extends Bundle<ContainerConfiguration> {

    /** Creates a new ContainerBundle. */
    protected ContainerBundle() {
        super(ContainerConfiguration.driver());
    }

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
     * Returns an unmodifiable view of the extensions that have been configured so far.
     * 
     * @return an unmodifiable view of the extensions that have been configured so far
     * @see ContainerConfiguration#extensions()
     * @see #use(Class)
     */
    protected final Set<Class<? extends Extension>> extensions() {
        return configuration().extensions();
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
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param <T>
     *            the type of the component
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    protected final <T> BeanConfiguration<T> install(Class<T> implementation) {
        return configuration().wire(BeanConfiguration.driver(implementation));
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * 
     * @param <T>
     *            the type of the component
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see BaseBundle#install(Factory)
     */
    protected final <T> BeanConfiguration<T> install(Factory<T> factory) {
        return configuration().wire(BeanConfiguration.driver(factory));
    }

    protected final StatelessConfiguration installHelper(Class<?> implementation) {
        return configuration().installStateless(implementation);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this bundle will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link BeanConfiguration} can be used to specify a specific parent.
     *
     * @param <T>
     *            the type of component to install
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> BeanConfiguration<T> installInstance(T instance) {
        return configuration().wire(BeanConfiguration.driverInstance(instance));
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
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     */
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        configuration().lookup(lookup);
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

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is first time this method has been called with the specified extension type. This method will instantiate an
     * extension of the specified type and retain it for future invocation.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if called from outside {@link #configure()}
     * @see ContainerConfiguration#use(Class)
     */
    protected final <T extends Extension> T use(Class<T> extensionType) {
        return configuration().use(extensionType);
    }

    protected final <C, I> C wire(ComponentClassDriver<C, I> driver, Class<? extends I> implementation, Wirelet... wirelets) {
        return configuration().wire(driver, implementation, wirelets);
    }

    protected final <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return configuration().wire(driver, wirelets);
    }

    protected final <C, I> C wire(ComponentFactoryDriver<C, I> driver, Factory<I> factory, Wirelet... wirelets) {
        return configuration().wire(driver, factory, wirelets);
    }

    protected final <C, I> C wireInstance(ComponentInstanceDriver<C, I> driver, I instance, Wirelet... wirelets) {
        return configuration().wireInstance(driver, instance, wirelets);
    }
}

//// Must be a assembly type wirelet
//// useWirelet()
//protected final <W extends Wirelet> Optional<W> wirelet(Class<W> type) {
//  return configuration().assemblyWirelet(type);
//}

//// De her conditional wirelet kom aldrig til at fungere godt. PGA ordering
///**
// * @param <W>
// * @param wireletType
// * @param predicate
// * @return stuff
// */
//// Should we add wirelet(Type, consumer) or Optional<Wirelet>
//final <W extends Wirelet> boolean ifWirelet(Class<W> wireletType, Predicate<? super W> predicate) {
//    // Mainly used for inheritable wirelets...
//    // Would be nice if pipeline = wirelet... Because then we could do
//    // ifWirelet(somePipeline, containsX) ->
//    // Which we can if the user implements Wirelet themself
//
//    // This should not really be the first tool you use...
//    // Yeah I think bundle.setFoo() is so much better????
//    // Not sure we want to encourage it....
//
//    // But its useful for extensions, no? Well only to override
//    // settings such as WebExtension.defaultPort(); <- but that's runtime
//    // I mean for
//    // The runtime then...
//    return false;
//}
