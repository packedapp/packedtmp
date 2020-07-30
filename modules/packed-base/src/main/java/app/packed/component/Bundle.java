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
 */
// T extends RealmConfiguration...
// We don't want any code except packed.base to receive any kind of Lookup objects
// so Either T extends that... or we have some secret backdoor call to the underlying ComponentConfiguration...
// so realm(Lookup) {((RealmConfig) configuration).realm(ddd);}
public abstract class Bundle<T> {

    /**
     * The configuration of the container. Is initial null configure has not yet been called. Then it is initialized which a
     * Configuration. Finally before returning from configure. The configuration is replaced with xxx.
     * <p>
     */
    // This fields can contain 4 different types. All updated in PackedContainerConfiguration#configure.
    @Nullable
    private Object configuration;

    /** The driver of this bundle. */
    final ComponentDriver<? extends T> driver;

    /**
     * Creates a new bundle using the supplied driver.
     * 
     * @param driver
     *            the driver to use for constructing the bundles configuration object
     */
    protected Bundle(ComponentDriver<? extends T> driver) {
        this.driver = requireNonNull(driver, "driver is null");
    }

    protected <X> Bundle(SourcedComponentDriver<X, ? extends T> driver, Class<X> implementation) {
        this.driver = null;
    }

    protected <X> Bundle(SourcedComponentDriver<X, ? extends T> driver, Factory<X> implementation) {
        this.driver = null;
    }

    protected <X> Bundle(SourcedComponentDriver<X, ? extends T> driver, X instance) {
        this.driver = null; // Wirelet bliver ikke specificeret her.. Fordi ComponentDriver ikke bruger det.
    }

    /**
     * Returns the configuration object that this bundle wraps.
     * 
     * @return the configuration object that this bundle wraps
     * @throws IllegalStateException
     *             if called from outside of {@link #configure()}
     */
    @SuppressWarnings("unchecked")
    protected final T configuration() {
        Object c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        } else if (c instanceof BundleConfiguration) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        } else {
            return (T) c;
        }
    }

    /** Configures the bundle. This method should never be invoked directly by the user. */
    protected abstract void configure();
}

// Maaske hedder det ikke en bundle som root???

// Bundle, protected final
// realm <-- protected metoder

// Assembly, public
// realm() <-- public metode

//A bundle encapsulates configuration of a component.
//Possible enhancing it with options.
//
//Captures a realm as well.
//
//Controls precisely what is exposed to users of the Bundle
//(Is typically provided to other users)
//
//Can be used exactly once.
//
//-----
//I want them to be part of the container
//and then replaced at runtime...
//
//Sounds strange that Extension is part of the container.
//But ContainerBundle is not
//
//Extensions are removed... possible replaced with an instance component
//----
//
//Always extended
//
//ActorBundle {
//
//}
