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
package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.container.ContainerConfiguration;
import app.packed.util.Nullable;

/**
 *
 */
// Bundle vs Configurator
//// Hmmm, altsaa vi har Bundle, fordi det er den letteste maade at inkapsle ting...
//// Maaske skal vi slet ikke supportere generiske configuratorer...
//// Problemet er dog de protectede metoder...

public abstract class AnyBundle {

    /** The configuration */
    private ContainerConfiguration configuration;

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
            throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /** Configures the bundle using the various inherited methods that are available. */
    protected abstract void configure();

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

    /**
     * Returns the name of the container or null if the name has not been set.
     *
     * @return the name of the container or null if the name has not been set
     * @see #setName(String)
     * @see ContainerConfiguration#setName(String)
     */
    @Nullable
    protected final String getName() {
        return configuration.getName();
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

    protected final void setName(@Nullable String name) {
        configuration.setName(name);
    }

    protected final <T extends Extension<T>> T use(Class<T> extensionType) {
        return configuration.use(extensionType);
    }

    // protected final ContainerLink wire(AnyBundle child, WiringOption... operations) {
    // return configuration().wire(child);
    // }

    /** Configures the bundle using the various methods from the inherited class. */
    // protected abstract void configure();

    /**
     * Returns a feature of the specified type
     * 
     * @param <T>
     *            the extension type
     * @param featureType
     *            the feature type
     * @return an extension of the specified type
     * @throws UnsupportedOperationException
     *             if no features of the specified type is supported
     */
    // Skal alle virkelig have adgang....
    protected final <T extends Extension<T>> T extendWith(Class<T> featureType) {
        throw new UnsupportedOperationException();
    }

    // /**
    // * The lookup object passed to this method is never made available through the public api. It is only used internally.
    // * Unless your private
    // *
    // * @param lookup
    // * the lookup object
    // * @see SimpleInjectorConfigurator#lookup(Lookup)
    // */
    // protected void lookup(Lookup lookup) {
    // requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
    // // stuff
    // }

    protected void lookup(Lookup lookup, Object lookupController) {
        // Ideen er at alle lookups skal godkendes at lookup controlleren...
        // Controller/Manager/LookupAccessManager
        // For module email, if you are paranoid.
        // You can specify a LookupAccessManager where every lookup access.
        // With both the source and the target. For example, service of type XX from Module YY in Bundle BB needs access to FFF
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
}
