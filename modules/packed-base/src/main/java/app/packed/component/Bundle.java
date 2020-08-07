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
import app.packed.container.ContainerBundle;
import app.packed.inject.Factory;
import packed.internal.component.BundleConfiguration;

/**
 * A bundle is a thin wrapper that encapsulates a {@link ComponentDriver} and a component's configuration. This class is
 * primary used through one of its subclasses such as {@link ContainerBundle}.
 * <p>
 * This class is not meant to be extended by ordinary users. But provides means for power users to extend the basic
 * functionality of Packed.
 * 
 * @param <C>
 *            the type of configuration this bundle wraps
 */
public abstract class Bundle<C> {

    /**
     * The configuration of this bundle. This field is "magically" set using methods handles from
     * {@link BundleConfiguration}.
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the bundle has not yet been used.
     * <li>the second item
     * <li>the third item
     * </ul>
     * <p>
     * Is initial null configure has not yet been called. Then it is initialized which a Configuration. Finally before
     * returning from configure. The configuration is replaced with xxx.
     */
    @Nullable
    private Object configuration;

    /** The driver of this bundle. Is read "magically" using methods handles from {@link BundleConfiguration}. */
    final ComponentDriver<? extends C> driver;

    /**
     * Creates a new bundle using the supplied driver.
     * 
     * @param driver
     *            the driver to use for constructing this bundle's configuration object
     */
    protected Bundle(ComponentDriver<? extends C> driver) {
        this.driver = requireNonNull(driver, "driver is null");
    }

    protected <S> Bundle(SourcedComponentDriver<S, ? extends C> driver, Class<S> implementation) {
        this.driver = null;
    }

    protected <S> Bundle(SourcedComponentDriver<S, ? extends C> driver, Factory<S> factory) {
        this.driver = null;
    }

    /**
     * @param <S>
     *            the type of instance
     * @param driver
     * @param instance
     *            the instance to wrap
     */
    protected <S> Bundle(SourcedComponentDriver<S, ? extends C> driver, S instance) {
        this.driver = null;
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
        } else if (c instanceof BundleConfiguration) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        } else {
            return (C) c;
        }
    }

    /** Configures the bundle. This method should never be invoked directly by the user. */
    protected abstract void configure();
}
