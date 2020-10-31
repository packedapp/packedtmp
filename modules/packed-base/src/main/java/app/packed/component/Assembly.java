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
import app.packed.bundle.BaseAssembly;
import packed.internal.component.BundleHelper;
import packed.internal.component.PackedComponentDriver;

/**
 * A bundle is a thin wrapper that encapsulates a {@link ComponentDriver} and the configuration of a component. This
 * class is primary used through one of its subclasses such as {@link BaseAssembly}.
 * <p>
 * This class is not meant to be directly extended by ordinary users. But provides means for power users to extend the
 * basic functionality of Packed.
 * 
 * @param <C>
 *            the underlying component configuration this bundle wraps
 */
// Build eller Assembly. syntes ikke det skal hedde bundle mere...
// you write build classes
// ComponentAssembly?
public abstract class Assembly<C> implements ComponentSystem {

    /**
     * The configuration of this bundle. This field is set via a VarHandle from {@link BundleHelper}.
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the bundle has not yet been consumed.
     * <li>Then it is initialized with the actual configurations object. Cas'ed to prevent
     * <li>Finally, a non-null placeholder is set to indicate that the bundle has been consumed
     * </ul>
     */
    @Nullable
    // Bundle: States-> Ready -> Assembling|Composing -> Consumed|Composed... Ready | Using | Used... Usable | Using | Used
    // Unconfigured/Configuring/Configured (Failed??? well et can't bee Configured if it's failed)
    // [afdf, state = Unusued]consuming|consumed]
    private Object configuration;

    /** The driver of this bundle. This field is read via a VarHandle from {@link BundleHelper}. */
    @SuppressWarnings("unused")
    private final PackedComponentDriver<? extends C> driver;

    /**
     * Creates a new bundle using the specified component driver.
     * 
     * @param driver
     *            the driver to use for constructing this bundle's configuration object
     */
    protected Assembly(ComponentDriver<? extends C> driver) {
        this.driver = requireNonNull((PackedComponentDriver<? extends C>) driver, "driver is null");
    }

    /**
     * Returns the configuration object that this bundle wraps.
     * <p>
     * This method must only be called from within the bounds of the {@link #build()} method.
     * 
     * @return the configuration object that this bundle wraps
     * @throws IllegalStateException
     *             if called from outside of {@link #build()}
     */
    @SuppressWarnings("unchecked")
    protected final C configuration() {
        Object c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        } else if (c == BundleHelper.BUNDLE_CONSUMED) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        } else {
            return (C) c;
        }
    }

    /** Configures the bundle. This method should never be invoked directly by the user. */
    protected abstract void build();
}
