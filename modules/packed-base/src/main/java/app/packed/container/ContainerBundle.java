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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import app.packed.component.ComponentBundle;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.component.WireletSidecar;
import app.packed.inject.Factory;
import app.packed.service.ServiceExtension;

/**
 * Bundles are the main source of configuration for containers and artifacts. Basically a bundle is just a thin wrapper
 * around {@link ContainerConfiguration}. Delegating every invocation in the class to an instance of
 * {@link ContainerConfiguration} available via {@link #configuration()}.
 * <p>
 * A bundle instance can be used ({@link #configure()}) exactly once. Attempting to use it multiple times will fail with
 * an {@link IllegalStateException}.
 * 
 * A generic bundle. Normally you would extend {@link BaseBundle}
 */

// Nej der er ingen grund til at lave den concurrent. Som regel er det en ny instans...

// Maybe introduce ContainerBundle()... Det jeg taenker er at introduce noget der f.eks. kan bruges i kotlin
// saa man kan noget der minder om https://ktor.io
// Altsaa en helt barebones bundle

// Kunne godt have nogle lifecycle metoder man kunne overskrive.
// F.eks. at man vil validere noget

public abstract class ContainerBundle extends ComponentBundle<ContainerConfiguration> {

    /** Creates a new ContainerBundle. */
    protected ContainerBundle() {
        super(ContainerConfiguration.driver());
    }

    /**
     * Returns an unmodifiable view of the extensions that have been configured so far.
     * 
     * @return an unmodifiable view of the extensions that have been configured so far
     * @see ContainerConfiguration#extensions()
     * @see #use(Class)
     */
    protected final Set<Class<? extends Extension>> extensions() {
        return configuration().extensions();
    }

    /**
     * @param <W>
     * @param wireletType
     * @param predicate
     * @return stuff
     * @throws IllegalArgumentException
     *             if the specified wirelet type does not have {@link WireletSidecar#failOnImage()} set to true
     */
    // Should we add wirelet(Type, consumer) or Optional<Wirelet>
    final <W extends Wirelet> boolean ifWirelet(Class<W> wireletType, Predicate<? super W> predicate) {
        // Mainly used for inheritable wirelets...
        // Would be nice if pipeline = wirelet... Because then we could do
        // ifWirelet(somePipeline, containsX) ->
        // Which we can if the user implements Wirelet themself

        // This should not really be the first tool you use...
        // Yeah I think bundle.setFoo() is so much better????
        // Not sure we want to encourage it....

        // But its useful for extensions, no? Well only to override
        // settings such as WebExtension.defaultPort(); <- but that's runtime
        // I mean for
        // The runtime then...
        @WireletSidecar(failOnImage = true)
        class MyWirelet implements Wirelet {}
        return false;
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
    protected final <T> SingletonConfiguration<T> install(Class<T> implementation) {
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
    protected final <T> SingletonConfiguration<T> install(Factory<T> factory) {
        return configuration().install(factory);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this bundle will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link SingletonConfiguration} can be used to specify a specific parent.
     *
     * @param <T>
     *            the type of component to install
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> SingletonConfiguration<T> installInstance(T instance) {
        return configuration().installInstance(instance);
    }

    protected final StatelessConfiguration installHelper(Class<?> implementation) {
        return configuration().installStateless(implementation);
    }

    /**
     * Returns whether or not this bundle will configure the top container in an artifact.
     * 
     * @return whether or not this bundle will configure the top container in an artifact
     * @see ContainerConfiguration#isArtifactRoot()
     */
    protected final boolean isTopContainer() {
        return configuration().isArtifactRoot();
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

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is first time this method has been called with the specified extension type. This method will instantiate an
     * extension of the specified type and retain it for future invocation.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if called from outside {@link #configure()}
     * @see ContainerConfiguration#use(Class)
     */
    protected final <T extends Extension> T use(Class<T> extensionType) {
        return configuration().use(extensionType);
    }

    // Must be a assembly type wirelet
    // useWirelet()
    protected final <W extends Wirelet> Optional<W> wirelet(Class<W> type) {
        return configuration().assemblyWirelet(type);
    }
}
