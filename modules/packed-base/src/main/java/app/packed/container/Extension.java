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

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.config.ConfigSite;
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
// ErrorHandle, Logging

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

// Disallow registering extensions as a service??? Actually together with a lot of other types...
// ContainerExtension vs ContainerPlugin
public abstract class Extension {

    static {
        AppPackedContainerSupport.Helper.init(new AppPackedContainerSupport.Helper() {

            @Override
            public void doConfigure(ContainerBundle bundle, ContainerConfiguration configuration) {
                bundle.doConfigure(configuration);
            }

            /** {@inheritDoc} */
            @Override
            public void initializeExtension(Extension extension, PackedContainerConfiguration configuration) {
                extension.configuration = requireNonNull(configuration);
                extension.onAdded();
            }

            /** {@inheritDoc} */
            @Override
            public void onConfigured(Extension extension) {
                extension.onConfigured();
                extension.isConfigurable = false;
            }
        });
    }

    /** The configuration of the container in which the extension is registered. */
    private ContainerConfiguration configuration;

    /** Whether or not the extension is configurable. */
    private boolean isConfigurable = true;

    /**
     * Captures the configuration site by finding the first stack frame that is not located on a subclass of
     * {@link Extension}.
     * <p>
     * Invoking this method typically takes in the order of 1-2 microseconds.
     * <p>
     * If stack frame based config has been disable via, for example, fooo. This method returns {@link ConfigSite#UNKNOWN}.
     * 
     * @return a configuration site
     * @see StackWalker
     */
    protected final ConfigSite configSiteCapture() {
        throw new UnsupportedOperationException();
    }

    public void buildBundle(BundleDescriptor.Builder builder) {}

    /**
     * Returns the build context for artifact that is being build.
     * <p>
     * Im thinking about throwing ISE on instantiation....
     * 
     * @throws IllegalStateException
     *             if invoked from constructor or {@link #onPrepareContainerInstantiate(ArtifactInstantiationContext)}.
     * @return the build context
     */
    protected final ArtifactBuildContext buildContext() {
        // The thing i'm worried about here is wirelets...
        // Because this should not be used on instantiation time because
        // Any wirelets specified when initializing an image is not included...
        // Or is this controllable from ContainerConfiguration????
        ArtifactBuildContext c = configuration().buildContext();
        if (!isConfigurable) {
            throw new IllegalStateException(
                    "This method can only be called while the container is being configured. After onConfigured() has returned the build ");
        }
        return c;
    }

    /**
     * Checks that the extension is still configurable or throws an {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after the extensions {@link #onConfigured()} method has returned.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable.
     */
    protected final void checkConfigurable() {
        configuration();// First check that we are not invoking this from the constructor of the extension
        if (!isConfigurable) {
            throw new IllegalStateException("This extension " + getClass().getSimpleName() + " is no longer configurable");
        }
    }

    /**
     * Returns the configuration of the container. Or fails with {@link IllegalStateException} if invoked from the
     * constructor of an extension.
     * 
     * @return the configuration of the container
     */
    private ContainerConfiguration configuration() {
        // When calling this method remember to add test to BasicExtensionTest
        ContainerConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException(
                    "This operation cannot be called from the constructor of the extension, #onAdd() can be overridden, as an alternative, to perform initialization");
        }
        return c;
    }

    protected final void installInParentIfSameArtifact() {
        // Alternativeet
        // useInParent????
    }

    // Sidecards per extension???
    // Det betyder jo ogsaa "endnu" mere magt til extensions..
    protected final void installSidecar(Object instance) {
        // These should work with images as well..

        // I virkeligheden er det jo paa ContainerConfiguration vi installere den....
        class SidecarConfiguration {

        }
        System.out.println(new SidecarConfiguration());
    }

    /**
     * This method is invoked (exactly once) by the runtime immediately after the extension is added to the configuration of
     * a container. For example, via a call to {@link ContainerConfiguration#use(Class)}. After this method has returned the
     * extension instance is returned to the user.
     * <p>
     * {@link #onConfigured()} is the next callback method invoked by the runtime.
     * <p>
     * The default implementation does nothing.
     */
    protected void onAdded() {} // afterAdd

    /**
     * Invoked immediately after a container has been successfully configured. Typically after
     * {@link ContainerBundle#configure()} has returned.
     * <p>
     * The default implementation does nothing.
     */
    protected void onConfigured() {} // afterConfigure,

    /**
     * Invoked whenever the container is being instantiated. In case of a container image this means that method might be
     * invoked multiple times. Even by multiple threads
     * 
     * @param context
     *            an instantiation context object
     */
    public void onPrepareContainerInstantiate(ArtifactInstantiationContext context) {}

    final void runWithLookup(Lookup lookup, Runnable runnable) {
        // Ideen er at vi kan installere component. o.s.v. med det specificeret lookup....
        // D.v.s. vi laver en push, pop af et evt. eksisterende lookup object
        // En install fra en extension skal jo naesten bruge denne..
        // Faktisk, er der lidt sikkerhedshullumhej her.... Hvordan sikre vi os at extensions.
        // Ikke goer noget sjovt her. Hmm, altsaa indvitere man en extension indenfor...

        // Men vi vel helst have at de giver adgang via module-info...

    }

    /**
     * Returns an extension of the specified type. Invoking this method is similar to calling
     * {@link ContainerConfiguration#use(Class)}.
     * <p>
     * 
     * The runtime keeps track of extensions usage of other extensions via this method. And forming any kind of circle in
     * the dependency graph will fail with a runtime exception.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the underlying container is no longer configurable and an extension of the specified type has not
     *             already been installed
     */
    protected final <E extends Extension> E use(Class<E> extensionType) {
        return configuration().use(extensionType);
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
