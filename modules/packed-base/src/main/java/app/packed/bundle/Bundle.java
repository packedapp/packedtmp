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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;

/**
 * Bundles provide a simply way to package components and service and build modular application. This is useful, for
 * example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link InjectorBundle}</b> which bundles information about services, and creates {@link Injector} instances
 * using {@link Injector#of(Class)}.</li>
 * <li><b>{@link ContainerBundle}</b> which bundles information about both services and components, and creates
 * {@link Container} instances using {@link Container#of(Class)}.</li>
 * </ul>
 */

// Descriptor does not freeze, Injector+Container freezes
public abstract class Bundle {

    /** Whether or not {@link #configure()} has been invoked. */
    boolean isFrozen;

    BundleSupport support;

    /**
     * Checks that the {@link #configure()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does try to configure the extension after it has been configured.
     *
     * <pre>{@code
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

    /** Configures the bundle using the various methods from the inherited class. */
    protected abstract void configure();

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     * @see InjectorConfiguration#lookup(Lookup)
     */
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        support().lookup(lookup);
    }

    /**
     * Returns the bundle support object which
     * 
     * @return the bundle support object
     */
    protected final BundleSupport support() {
        // Vi laver en bundle nyt per configuration.....
        BundleSupport s = support;
        if (s == null) {
            throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to call Bundle.configure directly");
        }
        return s;
    }

    // Class<?> RuntimeType
}
