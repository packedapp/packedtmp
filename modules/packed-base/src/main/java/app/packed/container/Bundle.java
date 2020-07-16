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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import app.packed.artifact.ArtifactSource;
import app.packed.artifact.hostguest.HostConfiguration;
import app.packed.artifact.hostguest.HostDriver;
import app.packed.base.Nullable;
import app.packed.component.ComponentPath;
import app.packed.component.ConfiguredBy;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.container.WireletPipelineContext;
import packed.internal.moduleaccess.AppPackedContainerAccess;
import packed.internal.moduleaccess.ModuleAccess;

/**
 * Bundles are the main source of configuration for containers and artifacts. Basically a bundle is just a thin wrapper
 * around {@link BundleConfiguration}. Delegating every invokation in the class to an instance of
 * {@link BundleConfiguration} available via {@link #configuration()}.
 * <p>
 * Once consumed a bundle cannot be used...
 * 
 * A generic bundle. Normally you would extend {@link BaseBundle}
 */
// Bundles and reusability, mulighed
// Ingen
// Repeatable, men ikke concurrently <--- Nej
// Concurrent

// Maybe introduce ContainerBundle()... Det jeg taenker er at introduce noget der f.eks. kan bruges i kotlin
// saa man kan noget der minder om https://ktor.io
// Altsaa en helt barebones bundle

// Kunne godt have nogle lifecycle metoder man kunne overskrive.
// F.eks. at man vil validere noget

// Bundle: States-> Ready -> Assembling|Composing -> Consumed|Composed... Ready | Using | Used... Usable | Using | Used

// Trying to use a bundle multiple types will result in an ISE
public abstract class Bundle implements ArtifactSource {

    static {
        ModuleAccess.initialize(AppPackedContainerAccess.class, new AppPackedContainerAccess() {

            /** {@inheritDoc} */
            @Override
            public void bundleConfigure(Bundle bundle, BundleConfiguration configuration) {
                bundle.doCompose(configuration);
            }

            /** {@inheritDoc} */
            @Override
            public void extensionSetContext(Extension extension, ExtensionConfiguration context) {
                extension.configuration = context;
            }

            /** {@inheritDoc} */
            @Override
            public void pipelineInitialize(WireletPipeline<?, ?> pipeline, WireletPipelineContext context) {
                pipeline.context = context;
                pipeline.verify();
            }
        });
    }

    /** The configuration of the container. Should only be read via {@link #configuration}. */
    private BundleConfiguration configuration;

    /** The state of the bundle. 0 not-initialized, 1 in-progress, 2 completed. */
    private final AtomicInteger state = new AtomicInteger();

    protected final <C> C add(Class<? extends ConfiguredBy<C>> type) {
        throw new UnsupportedOperationException();
    }

    // Do we need a provide host also????
    protected final <C extends HostConfiguration<?>> C addHost(HostDriver<C> driver) {
        return configuration().addHost(driver);
    }

    /**
     * Checks that the {@link #compose()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does not try to configure the extension after it has been configured.
     * 
     * @throws IllegalStateException
     *             if {@link #compose()} has been invoked
     * @see BundleConfiguration#checkConfigurable()
     */
    protected final void checkConfigurable() {
        configuration().checkConfigurable();
    }

    /**
     * Configures the bundle using the various methods that are available.
     * <p>
     * This method is intended to be invoked by the runtime. Users should normally <b>never</b> invoke this method directly.
     */
    protected abstract void compose();

    /**
     * Returns the configuration site of this bundle.
     * 
     * @return the configuration site of this bundle
     * @see BundleConfiguration#configSite()
     */
    protected final ConfigSite configSite() {
        return configuration().configSite();
    }

    /**
     * Returns the container configuration that this bundle wraps.
     * 
     * @return the container configuration that this bundle wraps
     * @throws IllegalStateException
     *             if called from outside of {@link #compose()}
     */
    protected final BundleConfiguration configuration() {
        BundleConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /**
     * Invoked by the runtime to start the configuration process.
     * 
     * @param configuration
     *            the configuration to wrap
     * @throws IllegalStateException
     *             if the bundle is in use, or has previously been used
     */
    private void doCompose(BundleConfiguration configuration) {
        int s = state.compareAndExchange(0, 1);
        if (s == 1) {
            throw new IllegalStateException("This bundle is being used elsewhere, bundleType = " + getClass());
        } else if (s == 2) {
            throw new IllegalStateException("This bundle has already been used, bundleType = " + getClass());
        }

        this.configuration = configuration;
        try {
            compose();
        } finally {
            state.set(2);
            this.configuration = null;
        }

        // Do we want to cache exceptions?
        // Do we want better error messages, for example, This bundle has already been used to create an artifactImage
        // Do we want to store the calling thread in case of recursive linking..

        // Im not sure we want to null it out...
        // We should have some way to mark it failed????
        // If configure() fails. The ContainerConfiguration still works...
        /// Well we should probably catch the exception from where ever we call his method
    }

    /**
     * Returns an unmodifiable view of the extensions that have been used.
     * 
     * @return an unmodifiable view of the extensions that have been used
     * @see BundleConfiguration#extensions()
     * @see #use(Class)
     */
    protected final Set<Class<? extends Extension>> extensions() {
        return configuration().extensions();
    }

    /**
     * Returns any description that has been set.
     * 
     * @return any description that has been set
     * @see #setDescription(String)
     * @see BundleConfiguration#getDescription()
     */
    @Nullable
    protected final String getDescription() {
        return configuration().getDescription();
    }

    /**
     * Returns the name of the container. If no name has previously been set via {@link #setName(String)} a name is
     * automatically generated by the runtime as outlined in {@link #setName(String)}.
     * <p>
     * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
     * being thrown.
     * 
     * @return the name of the container
     * @see #setName(String)
     * @see BundleConfiguration#setName(String)
     */
    protected final String getName() {
        return configuration().getName();
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
     * @see BundleConfiguration#isArtifactRoot()
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
     * @see BundleConfiguration#link(Bundle, Wirelet...)
     */
    protected final void link(Bundle bundle, Wirelet... wirelets) {
        configuration().link(bundle, wirelets);
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     * @see BundleConfiguration#lookup(Lookup)
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

    /**
     * Returns the full path of the container that this bundle creates.
     * 
     * @return the full path of the container that this bundle creates
     * @see BundleConfiguration#path()
     */
    protected final ComponentPath path() {
        return configuration().path();
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
     * Sets the description of the container.
     * 
     * @param description
     *            the description to set
     * @see BundleConfiguration#setDescription(String)
     * @see #getDescription()
     */
    protected final void setDescription(String description) {
        configuration().setDescription(description);
    }

    /**
     * Sets the name of the container. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * This method should be called as the first thing when configuring a container.
     * <p>
     * If no name is set using this method. A name will be assigned to the container when the container is initialized, in
     * such a way that it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the container
     * @see #getName()
     * @see BundleConfiguration#setName(String)
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @throws IllegalStateException
     *             if called from outside {@link #compose()}
     */
    protected final void setName(String name) {
        configuration().setName(name);
    }

    /**
     * Returns an extension of the specified type. Instantiating and registering one for subsequent calls, if one has not
     * already been registered.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if called from outside {@link #compose()}
     * @see BundleConfiguration#use(Class)
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
