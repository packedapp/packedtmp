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

import app.packed.util.Nullable;

/**
 * A generic bundle. Normally you would extend {@link Bundle}
 */

// A bundle can be used by one thread at a time...
// However, once configured once. It cannot be changed...
// Saa dette burde virke
// Bundle b = new SomeBundle();
// wire(b, setName("f1"));
// wire(b, setName("f2"));
public abstract class AnyBundle {

    /** The configuration. */
    private ContainerConfiguration configuration;

    /** Whether or not {@link #configure()} has been invoked. */
    boolean isFrozen;

    /**
     * Checks that the {@link #configure()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does try to configure the extension after it has been configured.
     *
     * <pre>
     * {@code
     * public ManagementBundle setJMXEnabled(boolean enabled) {
     *     checkConfigurable(); //will throw IllegalStateException if configure() has already been called
     *     this.jmxEnabled = enabled;
     *     return this;
     * }}
     * </pre>
     * 
     * @throws IllegalStateException
     *             if the {@link #configure()} method has already been invoked once for this extension instance
     */
    protected final void checkConfigurable() {
        if (isFrozen) {
            // throw new IllegalStateException("This bundle is no longer configurable");
        }
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
                    "This method can only be called from within a bundles #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /** Configures the bundle using the various inherited methods that are available. */
    protected void configure() {}

    public final void doConfigure(ContainerConfiguration configuration) {
        this.configuration = configuration;
        try {
            configure();
        } finally {
            configuration = null;
        }
    }

    protected final Set<Class<? extends Extension<?>>> extensionTypes() {
        return configuration.extensionTypes();
    }

    @Nullable
    protected final String getDescription() {
        return configuration().getDescription();
    }

    /**
     * Returns the name of the container or null if the name has not been set.
     *
     * @return the name of the container or null if the name has not been set
     * @see #setName(String)
     * @see ContainerConfiguration#getName()
     */
    @Nullable
    protected final String getName() {
        return configuration.getName();
    }

    protected final <T extends AnyBundle> T link(T bundle, Wirelet... wirelets) {
        return configuration.link(bundle, wirelets);
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

    final void lookup(Lookup lookup, Object lookupController) {
        // Ideen er at alle lookups skal godkendes at lookup controlleren...
        // Controller/Manager/LookupAccessManager
        // For module email, if you are paranoid.
        // You can specify a LookupAccessManager where every lookup access.
        // With both the source and the target. For example, service of type XX from Module YY in Bundle BB needs access to FFF
    }

    protected final ContainerConfiguration setDescription(@Nullable String description) {
        return configuration().setDescription(description);
    }

    /**
     * Sets the {@link Container#name() name} of the container. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
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
    protected final void setName(@Nullable String name) {
        configuration.setName(name);
    }

    protected final <T extends Extension<T>> T use(Class<T> extensionType) {
        return configuration.use(extensionType);
    }
}

// alternative is some kind of builder....

// installXX
// Der er lidt bootstrap metoder...for staaet paa den maade, at man kan saette lidt ting op.
// som bliver koert inde configure(), og nogle ting der bliver koert efter....
// Also, som finishItem configuration

// Ville vaere saa sindsygt, hvis vi kunne definere Bundle (BaseBundle) paa den her maade
// Og med en api, der kan bruges af andre ogsaa...
// skal vi have en spi pakke??? nahhhhh

// Layer!...! Aahhh shit det bliver noget meta hullumhej...
