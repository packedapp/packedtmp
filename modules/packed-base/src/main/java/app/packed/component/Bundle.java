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

import app.packed.base.Nullable;
import packed.internal.component.BundleConfiguration;

/**
 * A bundle is a thin wrapper that encapsulates a {@link WireableComponentDriver} and the configuration of a component.
 * This class is primary used through one of its subclasses such as .
 * <p>
 * This class is not meant to be extended by ordinary users. But provides means for power users to extend the basic
 * functionality of Packed.
 * 
 * @param <C>
 *            the underlying configuration this bundle wraps
 */
public abstract class Bundle<C> implements AnalysableSystem {

    /**
     * The configuration of this bundle. This field is "magically" set via a var handle from {@link BundleConfiguration}.
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the bundle has not yet been consumed.
     * <li>Then it is initialized with the actual configurations object. Cas'ed to prevent
     * <li>Finally, a non-null placeholder is set to indicate that the bundle has been consumed
     * </ul>
     */
    @Nullable
    private Object configuration;

    /** The driver of this bundle. Is "magically" read via a var handle from {@link BundleConfiguration}. */
    @SuppressWarnings("unused")
    // Bundle: States-> Ready -> Assembling|Composing -> Consumed|Composed... Ready | Using | Used... Usable | Using | Used
    // Unconfigured/Configuring/Configured (Failed??? well et can't bee Configured if it's failed)
    // [afdf, state = Unusued]consuming|consumed]
    private final WireableComponentDriver<? extends C> driver; // TODO maybe use for tostring??? Include state

    /**
     * Creates a new bundle using the specified driver.
     * 
     * @param driver
     *            the driver to use for constructing this bundle's configuration object
     */
    protected Bundle(WireableComponentDriver<? extends C> driver) {
        this.driver = requireNonNull(driver, "driver is null");
    }

    /**
     * Returns the configuration object that this bundle wraps.
     * <p>
     * This method must only be called from within the bounds of the {@link #configure()} method.
     * 
     * @return the configuration object that this bundle wraps
     * @throws IllegalStateException
     *             if called from outside of {@link #configure()}
     */
    @SuppressWarnings("unchecked")
    protected final C configuration() {
        Object c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        } else if (c == BundleConfiguration.CONSUMED_SUCCESFULLY) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        } else {
            return (C) c;
        }
    }

    /** Configures the bundle. This method should never be invoked directly by the user. */
    protected abstract void configure();
}
