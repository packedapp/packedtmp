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

import app.packed.artifact.ArtifactSource;
import app.packed.component.Bundle;
import app.packed.component.ComponentBundle;
import app.packed.component.ComponentDriver;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.component.WireletSidecar;
import app.packed.component.driver.SingletonComponentDriver;
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
 * A generic bundle. Normally you would extend {@link DefaultBundle}
 */

// Nej der er ingen grund til at lave den concurrent. Som regel er det en ny instans...

// Maybe introduce ContainerBundle()... Det jeg taenker er at introduce noget der f.eks. kan bruges i kotlin
// saa man kan noget der minder om https://ktor.io
// Altsaa en helt barebones bundle

// Kunne godt have nogle lifecycle metoder man kunne overskrive.
// F.eks. at man vil validere noget

// Bundle: States-> Ready -> Assembling|Composing -> Consumed|Composed... Ready | Using | Used... Usable | Using | Used

// Unconfigured/Configuring/Configured (Failed??? well et can't bee Configured if it's failed)
public abstract class ContainerBundle extends ComponentBundle<ContainerConfiguration> implements ArtifactSource {

    /** Creates a new ContainerBundle. */
    protected ContainerBundle() {
        this(ContainerConfiguration.driver());
    }

    protected ContainerBundle(ComponentDriver<? extends ContainerConfiguration> driver) {
        super(driver);
    }

    // Do we need a provide host also????
    protected final <C> C add(ComponentDriver<C> driver) {
        throw new UnsupportedOperationException();
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
     * 
     * 
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
     * @see DefaultBundle#install(Factory)
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
    protected final <T> SingletonConfiguration<T> installConstant(T instance) {
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
     * Links the specified bundle as a child to this bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            an optional array of wirelets
     * @see ContainerConfiguration#link(Bundle, Wirelet...)
     */
    protected final void link(Bundle<?> bundle, Wirelet... wirelets) {
        configuration().link(bundle, wirelets);
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

        // Its always an extension, its always a member, And there is probably a field hook of some kind
        // Packed will access a constructor
    }

//
//    protected final <A, H, C> C provideHost(OldHostDriver<A, H, C> driver) {
//        return configuration().addHost(driver);
//    }
//
//    protected final <A, H, C> C provideHost(OldHostDriver<A, H, C> driver, Key<? super A> key) {
//        return configuration().addHost(driver);
//    }

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

    protected final <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    protected final <C, X> C wire(SingletonComponentDriver<C, X> driver, Class<X> implementation, Wirelet... wirelets) {
        return wire(driver.source(implementation), wirelets);
    }

    protected final <C, X> C wire(SingletonComponentDriver<C, X> driver, Factory<X> factory, Wirelet... wirelets) {
        return wire(driver.source(factory), wirelets);
    }

    protected final <C, X> C wireInstance(SingletonComponentDriver<C, X> driver, X instance, Wirelet... wirelets) {
        return wire(driver.sourceInstance(instance), wirelets);
    }
}

///**
//* Invoked by the runtime to start the configuration process.
//* 
//* @param configuration
//*            the configuration to wrap
//* @throws IllegalStateException
//*             if the bundle is in use, or has previously been used
//*/
//private void doConfigure(ContainerConfiguration configuration) {
//  requireNonNull(configuration, "configuration is null");
//
//  // We perform a compare and exchange with configuration. Guarding against
//  // concurrent usage of this bundle.
//  Object prev = CONFIGURATION.compareAndExchange(this, null, configuration);
//  if (prev == null) {
//      try {
//          configure();
//      } finally {
//          CONFIGURATION.setVolatile(this, BundleHelper.POST_CONFIGURE);
//      }
//  } else if (prev instanceof ComponentConfiguration) {
//      // Can be this thread or another thread that is already using the bundle.
//      throw new IllegalStateException("This bundle is being used elsewhere, bundleType = " + getClass());
//  } else {
//      // Bundle has already been used succesfullly or unsuccesfully
//      throw new IllegalStateException("This bundle has already been used, bundleType = " + getClass());
//  }
//
//  // Do we want to cache exceptions?
//  // Do we want better error messages, for example, This bundle has already been used to create an artifactImage
//  // Do we want to store the calling thread in case of recursive linking..
//
//  // We should have some way to mark it failed????
//  // If configure() fails. The ContainerConfiguration still works...
//  /// Well we should probably catch the exception from where ever we call his method
//}