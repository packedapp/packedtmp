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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.container.Extension;
import app.packed.inject.Factory;
import packed.internal.component.ComponentBuild;

/**
 *
 */
// Can take a CCC context. And cast it and provide lookup??
// Maaske er det altid en container????
// This class should be inlign with Assembly so Either ComponentComposer or just Composer
//Composer<T>?
// Maaske skal Composer bare vaere en special Assembly...
// Jamen, saa skal vi tage noget andet end Assembly til de forskellige metoder.
// Assembly extended by Composer|
// Class<?> configurator = Assembly|Composer | Extension????<-- naah Assembly|Composer  
public abstract class Composer<C extends ComponentConfiguration> extends Realm {

    // I don't think we have access to component configuration context...
    /** The underlying component configuration. */
    protected final C configuration;

    protected Composer(C configuration) {
        this.configuration = requireNonNull(configuration, "configuration is null");
    }

    protected final void checkConfigurable() {
        configuration.context.checkConfigurable();
    }

    /**
     * Sets a {@link Lookup lookup object} that will be used to access members (fields, constructors and methods) on
     * registered objects. The lookup object will be used for all service bindings and component installations that happens
     * after the invocation of this method.
     * <p>
     * This method can be invoked multiple times. In all cases the object being bound or installed will use the latest
     * registered lookup object.
     * <p>
     * Lookup objects that have been explicitly set using {@link Factory#withLookup(java.lang.invoke.MethodHandles.Lookup)}
     * are never overridden by any lookup object set by this method.
     * <p>
     * If no lookup is specified using this method, the runtime will use the public lookup object
     * ({@link MethodHandles#publicLookup()}) for member access.
     *
     * @param lookup
     *            the lookup object
     */
    public final void lookup(MethodHandles.Lookup lookup) {
        ((ComponentBuild) configuration.context).realm.lookup(lookup);
    }

    @SafeVarargs
    protected static void $AllowExtensions(Class<? extends Extension>... extensions) {
        throw new UnsupportedOperationException();
    }

    // Eller ogsaa har vi en anden driver??? ComposerDriver...

    // Hmm. vi kan jo godt have flere forskellige configurationer...
    // Altsaa fx tillader vi ikke andre extensions hvis vi laver en ServiceLocator i nogen tilfaelde
    // Mens vi nok goere det i andre...
    // Men

    // De her bliver kaldt fra en statisks initializer
    // Ikke hvis man skal bruge en ArtifactDriver...
    @SafeVarargs
    protected static void $RejectExtensions(Class<? extends Extension>... extensions) {
        throw new UnsupportedOperationException();
    }
}
