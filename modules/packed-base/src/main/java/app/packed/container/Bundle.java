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
import java.util.concurrent.atomic.AtomicInteger;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.lang.Nullable;
import app.packed.service.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.moduleaccess.AppPackedContainerAccess;
import packed.internal.moduleaccess.ModuleAccess;

/**
 * Bundles are the main source of configuration for containers and artifacts. Basically a bundle is just a thin wrapper
 * around {@link ContainerConfiguration}. Delegating all calls to container configurations.
 * <p>
 * Once consumed a bundle cannot be used...
 * 
 * A generic bundle. Normally you would extend {@link BaseBundle}
 */
// Bundles and reusability, mulighed
// Ingen
// Repeatable, men ikke concurrently <--- Nej
// Concurrent

// Maybe introduce ContainerBundle()... Det jeg taenker er at introduce noget der f.eks. kan bruges i kotlin
// saa man kan noget der minder om https://ktor.io
// Altsaa en helt barebones bundle

// Kunne godt have nogle lifecycle metoder man kunne overskrive.
// F.eks. at man vil validere noget
public abstract class Bundle implements ContainerSource {

    static {
        ModuleAccess.initialize(AppPackedContainerAccess.class, new AppPackedContainerAccess() {

            /** {@inheritDoc} */
            @Override
            public void doConfigure(Bundle bundle, ContainerConfiguration configuration) {
                bundle.doConfigure(configuration);
            }
        });
    }

    /** The configuration of the container. */
    private ContainerConfiguration configuration;

    /** The state of the bundle. 0 not-initialized, 1 in-progress, 2 completed. */
    private final AtomicInteger state = new AtomicInteger();

    /**
     * Checks that the {@link #configure()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does not try to configure the extension after it has been configured.
     *
     * <pre>
     * {@code
     * public void setJMXEnabled(boolean enabled) {
     *     requireConfigurable(); //will throw IllegalStateException if configure() has already been called
     *     this.jmxEnabled = enabled;
     * }}
     * </pre>
     * 
     * @throws IllegalStateException
     *             if the {@link #configure()} method has already been invoked once for this extension instance
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
        return configuration.configSite();
    }

    // /**
    // * Returns the build context. A single build context object is shared among all containers for the same artifact.
    // *
    // * @return the build context
    // * @see ContainerConfiguration#buildContext()
    // */
    // protected final ArtifactBuildContext buildContext() {
    // return configuration.buildContext();
    // }

    /**
     * Returns the container configuration that this bundle wraps.
     * 
     * @return the container configuration that this bundle wraps
     * @throws IllegalStateException
     *             if called outside {@link #configure()}
     */
    protected final ContainerConfiguration configuration() {
        ContainerConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException(
                    "This method can only be called from within this bundles #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /**
     * Configures the bundle using the various methods that are available.
     * <p>
     * This method is intended to be invoked by the runtime. Users should normally <b>never</b> invoke this method directly.
     */
    protected abstract void configure();

    /**
     * Invoked by the runtime to start the configuration process.
     * 
     * @param configuration
     *            the configuration to wrap
     * @throws IllegalStateException
     *             if the bundle is in use, or has previously been used
     */
    private void doConfigure(ContainerConfiguration configuration) {
        int s = state.compareAndExchange(0, 1);
        if (s == 1) {
            throw new IllegalStateException("This bundle is being used elsewhere.");
        } else if (s == 2) {
            throw new IllegalStateException("This bundle cannot be reused.");
        }

        this.configuration = configuration;
        try {
            configure();
        } finally {
            state.set(2);
            this.configuration = null;
        }

        // Do we want to cache exceptions?
        // Do we want better error messages, for example, This bundle has already been used to create an artifactImage
        // Do we want to store the calling thread in case of recursive linking..

        // Im not sure we want to null it out...
        // We should have some way to mark it failed????
        // If configure() fails. The ContainerConfiguration still works...
        /// Well we should probably catch the exception from where ever we call his method
    }

    /**
     * Returns an unmodifiable set view of the extensions that are currently in use by the container.
     * 
     * @return an unmodifiable set view of the extensions that are currently in use by the container
     * @see ContainerConfiguration#extensions()
     */
    protected final Set<Class<? extends Extension>> extensions() {
        return configuration().extensions();
    }

    @Nullable
    protected final String getDescription() {
        return configuration().getDescription();
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
     * @see ComponentConfiguration#setName(String)
     */
    protected final String getName() {
        return configuration().getName();
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * <p>
     * This method uses the {@link ServiceExtension} to instantiate the an instance of the component. (only if there are
     * dependencies???)
     * 
     * @param <T>
     *            the type of the component
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    // Den eneste grund for at de her metoder ikke er paa ComponentConfiguration er actors
    // Eller i andre situation hvor man ikke vil have at man installere alm componenter..
    // Men okay. Maaske skal man wrappe det saa. Det er jo let nok at simulere med useParent
    protected final <T> ComponentConfiguration<T> install(Class<T> implementation) {
        return configuration().install(implementation);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * This method uses the {@link ServiceExtension} to instantiate an component instance from the factory.
     * 
     * @param <T>
     *            the type of the component
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see BaseBundle#install(Factory)
     */
    protected final <T> ComponentConfiguration<T> install(Factory<T> factory) {
        return configuration().install(factory);
    }

    protected final <T> ComponentConfiguration<T> installHelper(Class<T> implementation) {
        return configuration().installStateless(implementation);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this bundle will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link ComponentConfiguration} can be used to specify a specific parent.
     *
     * @param <T>
     *            the type of component to install
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> ComponentConfiguration<T> installInstance(T instance) {
        return configuration().installInstance(instance);
    }

    /**
     * Returns whether or not this bundle will configure the top container in an artifact.
     * 
     * @return whether or not this bundle will configure the top container in an artifact
     * @see ContainerConfiguration#isArtifactRoot()
     */
    protected final boolean isTopContainer() {
        return configuration.isArtifactRoot();
    }

    /**
     * Links the specified bundle to this bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            an optional array of wirelets
     * @see ContainerConfiguration#link(Bundle, Wirelet...)
     */
    protected final void link(Bundle bundle, Wirelet... wirelets) {
        configuration.link(bundle, wirelets);
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     * @see ContainerConfiguration#lookup(Lookup)
     */
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        configuration().lookup(lookup);
    }

    final void lookup(Lookup lookup, Object lookupController) {
        // Ideen er at alle lookups skal godkendes at lookup controlleren...
        // Controller/Manager/LookupAccessManager
        // For module email, if you are paranoid.
        // You can specify a LookupAccessManager where every lookup access.
        // With both the source and the target. For example, service of type XX from Module YY in Bundle BB needs access to FFF
    }

    protected final ContainerLayer newLayer(String name, ContainerLayer... dependencies) {
        // Why is this not in Bundle????
        return configuration().newLayer(name, dependencies);
    }

    /**
     * Returns the full path of the container that this bundle creates.
     * 
     * @return the full path of the container that this bundle creates
     * @see ContainerConfiguration#path()
     */
    protected final ComponentPath path() {
        return configuration().path();
    }

    /**
     * Sets the description of the container.
     * 
     * @param description
     *            the description to set
     * @see ContainerConfiguration#setDescription(String)
     */
    protected final void setDescription(String description) {
        configuration().setDescription(description);
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
     *             if calling this method after
     */
    protected final void setName(String name) {
        configuration().setName(name);
    }

    /**
     * Returns an extension of the specified type. Instantiating and registering one for subsequent calls, if one has not
     * already been registered.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @see ContainerConfiguration#use(Class)
     */
    protected final <T extends Extension> T use(Class<T> extensionType) {
        return configuration().use(extensionType);
    }
}
