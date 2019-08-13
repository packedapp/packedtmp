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

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactSource;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.util.Nullable;

/**
 * Bundles are the main source of configuration for artifacts. It is basically just a thin wrapper around
 * {@link ContainerConfiguration}.
 * 
 * 
 * A generic bundle. Normally you would extend {@link BaseBundle}
 */

// A bundle can be used by one thread at a time...
// However, once configured once. It cannot be changed...
// Saa dette burde virke
// Bundle b = new SomeBundle();
// wire(b, setName("f1"));
// wire(b, setName("f2"));
public abstract class Bundle implements ArtifactSource {

    /** The configuration of the container. */
    private ContainerConfiguration configuration;

    /**
     * Returns the build context. A single build context object is shared among all containers for the same artifact.
     * 
     * @return the build context
     * @see ContainerConfiguration#buildContext()
     */
    protected final ArtifactBuildContext buildContext() {
        return configuration.buildContext();
    }

    /**
     * Checks that the {@link #configure()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does not try to configure the extension after it has been configured.
     *
     * <pre>
     * {@code
     * public void setJMXEnabled(boolean enabled) {
     *     checkConfigurable(); //will throw IllegalStateException if configure() has already been called
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
     * Returns the configuration site that created this bundle.
     * 
     * @return the configuration site that created this bundle
     * @see ContainerConfiguration#configSite()
     */
    protected final ConfigSite configSite() {
        return configuration.configSite();
    }

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
     * Configures the bundle using the various inherited methods that are available.
     * <p>
     * Users should <b>never</b> invoke this method directly. Instead letting the runtime invoke it.
     */
    protected void configure() {}

    /**
     * Invoked by the runtime to start the configuration process.
     * 
     * @param configuration
     *            the configuration to wrap
     */
    final void doConfigure(ContainerConfiguration configuration) {
        this.configuration = configuration;
        // Im not sure we want to null it out...
        // We should have some way to mark it failed????
        // If configure() fails. The ContainerConfiguration still works...
        /// Well we should probably catch the exception from where ever we call his method
        try {
            configure();
        } finally {
            this.configuration = null;
        }
    }

    /**
     * Returns an immutable view of all of the extension types that are used by this bundle.
     * 
     * @return an immutable view of all of the extension types that are used by this bundle
     * 
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
     * Returns whether or not this bundle will configure the top container in an artifact.
     * 
     * @return whether or not this bundle will configure the top container in an artifact
     * @see ContainerConfiguration#isTopContainer()
     */
    protected final boolean isTopContainer() {
        return configuration.isTopContainer();
    }

    /**
     * Links the specified bundle to this bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            an optional array of wirelets
     * @see ComponentExtension#link(Bundle, Wirelet...)
     */
    protected final void link(Bundle bundle, Wirelet... wirelets) {
        use(ComponentExtension.class).link(bundle, wirelets);
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

    protected final WireletList wirelets() {
        return configuration.wirelets();
    }
}
