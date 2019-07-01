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
package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.util.AttachmentMap;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.support.AppPackedContainerSupport;

/**
 * Container extensions are used to extend containers with functionality.
 * <p>
 * Subclasses of this class must give open rights to app.packed.base
 *
 * <p>
 * Subclasses of this class that are actively used should be final.
 */
// CurrentState???
// ErrorHandling / Notifications ???
/// Taenker det ligger paa Extension'en fordi vi har jo ogsaa en InstantiationContext
// hvor errors jo ogsaa kan ske..
// hasErrors()...
//// Maybe we want to log the actual extension as well.
// so extension.log("fooo") instead
/// Yes, why not use it to log errors...
// Den eneste ting jeg kunne forstille mig at kunne vaere public.
// Var en maade at se paa hvordan en extension blev aktiveret..
// Men er det ikke bare noget logning istedet for metoder...
// "InjectorExtension:" Activate
//// Her er der noget vi gerne vil have viral.

// Disallow registering extensions as a service???
// Actually together with a lot of other types...

// Feature
// Configurator

// ContainerExtension, ContainerPlugin
// Plugin (maybe todays favorite)
public abstract class ContainerExtension<T extends ContainerExtension<T>> {

    static {
        AppPackedContainerSupport.Helper.init(new AppPackedContainerSupport.Helper() {

            @Override
            public void doConfigure(ContainerBundle bundle, ContainerConfiguration configuration) {
                bundle.doConfigure(configuration);
            }

            /** {@inheritDoc} */
            @Override
            public void initializeExtension(ContainerExtension<?> extension, PackedContainerConfiguration configuration) {
                extension.configuration = requireNonNull(configuration);
                extension.onExtensionAdded();
            }
        });
    }

    /** The configuration of the container in which the extension is registered. */
    private PackedContainerConfiguration configuration;

    public void buildBundle(BundleDescriptor.Builder builder) {}

    /**
     * <p>
     * Im thinking about throwing ISE on instantiation....
     * 
     * @throws IllegalStateException
     *             if invoked from constructor or {@link #onPrepareContainerInstantiate(InstantiationContext)}.
     * @return the build context
     */
    protected final ArtifactBuildContext buildContext() {
        return configuration().buildContext();
    }

    /**
     * Checks that the container that this extension belongs to is still configurable. Throwing an
     * {@link IllegalStateException} if it is not.
     * 
     * @throws IllegalStateException
     *             if the container this extension is no longer configurable.
     * @return the configuration of the container
     */
    protected final ContainerConfiguration checkConfigurable() {
        ContainerConfiguration c = configuration();
        c.checkConfigurable();
        return c;
    }

    /**
     * Returns the configuration of the container. Or fails with {@link IllegalStateException} if invoked from the
     * constructor of an extension.
     * 
     * @return the configuration of the container
     */
    protected final ContainerConfiguration configuration() {
        PackedContainerConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException(
                    "This operation cannot be called from the constructor of the extension, #onAdd() can be overridden to perform initialization as an alternative");
        }
        return c;
    }

    /**
     * If this extensions container is deployed into a host, returns the host.
     * 
     * @return attachments
     */
    protected final Optional<AttachmentMap> host() {
        // Ideen er at hvis man har en host som foraeldre saa....
        // Skal den vaere tilgaengelig fra ContainerConfiguration??
        // Skal vi have et egentligt interface??? Kunne jo ogsaa vaere rart med en path??
        // Maaske at kunne se kontrakterne... o.s.v.
        throw new UnsupportedOperationException();
        // host().
    }

    /**
     * Invoked immediately after a container has been configured. Typically after {@link ContainerBundle#configure()} has
     * returned.
     * 
     * <p>
     * The default implementation does nothing.
     */
    public void onContainerConfigured() {}

    /**
     * This method is invoked exactly once by the runtime immediately after the extension is added to a container
     * configuration. And before the extension is made available to other extensions or users.
     */
    protected void onExtensionAdded() {}

    /**
     * Invoked whenever the container is being instantiated. In case of a container image this means that method might be
     * invoked multiple times. Even by multiple threads
     * 
     * @param context
     *            an instantiation context object
     */
    public void onPrepareContainerInstantiate(InstantiationContext context) {}

    /**
     * If the underlying container has parent which uses this container, returns the parents
     * 
     * @return if the underlying any parent of this container
     * @throws IllegalStateException
     *             if called from outside of {@link #onContainerConfigured()}.
     */
    protected final Optional<T> parent() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the path of the underlying container.
     * 
     * @return the path of the underlying container
     */
    protected final ComponentPath path() {
        return configuration().path();
    }

    // Skal have en eller anden form for link med...
    // Hvor man kan gemme ting. f.eks. en Foo.class
    // Det er ogsaa her man kan specificere at et bundle har en dependency paa et andet bundle
    // protected void onWireChild(@Nullable T child, BundleLink link) {}
    //
    // // onWireChikd
    // protected void onWireParent(@Nullable T parent, BundleLink link) {}

    /**
     * Creates a new configuration site
     * 
     * <p>
     * If the gathering of a stack-based configuration is disabled. This method return {@link ConfigSite#UNKNOWN}.
     * 
     * @param name
     *            the name of the operation
     * @return the new configuration site
     */
    protected final ConfigSite spawnConfigSite(String name) {
        // Have a depth indicator.....
        // + ConfigSiteStackFilter.create("com.acme")
        // ConfigSite spawnConfigSite(ConfigSiteStackFilter f, String name) {} Den ved alt om config sites er disablet paa
        // containeren
        throw new UnsupportedOperationException();
    }

    public final <E extends ContainerExtension<E>> E use(Class<E> extensionType) {
        return configuration.use(extensionType);
    }

    /**
     * Returns a list of any wirelets that was used to configure the container.
     * <p>
     * Invoking this method is equivalent to invoking {@code configuration().wirelets()}.
     * 
     * @return a list of any wirelets that was used to configure the container
     * 
     */
    protected final WireletList wirelets() {
        return configuration().wirelets();
    }
}

// protected final void newLine() {
// checksConfigurable
// FreezesAnyNode before

// Checks that the bundle/configurator/... is still active
// Freezes any previous node for modifications....
// Which means that everything is nodes....

// Because bind(x) followed by install(x) should work identical to
// Because install(x) followed by bind(x) should work identical to
// }

// createContract(); or
// addToContract(ContractBuilder b)
// Failure to have two features creating the same contract type...

// If it uses other extensions... Either get them by constructor, or via use(xxx)
/// F.eks. LifecycleExtension

// We should probably have some check that an extension is not used multiple places.../
// But then again maybe it is allowed to

// maybe a featureSet(FeatureInstaller fi) {
// } that can be overridden....

// public static final JAVAX_INJECT
// public static final DEFAULT (Hierachical error handling)....
// You can have services without inject, but not the other way around...
// featuresFreeze() <- used in a super class to prevent subclasses to add/change features...

// LifeFeature.withRequireDependenciesToBeFullyStarted

// Vi bliver noedt til at gemme noget live ogsaa, Hvis vi laver en host....
// Men man kan søge rekursivt efter det...

// Cache Information

// Provide Contract, Descriptor, .... (HookContract, HookDescriptor)....

// Direction of information, @OnHook creates a dependendy TO

// Allow filters..., incoming wiring, outgoing wiring
