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
package app.packed.container.extension;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.Optional;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerSource;
import app.packed.container.WireletList;
import packed.internal.access.AppPackedExtensionAccess;
import packed.internal.access.SharedSecrets;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.extension.PackedExtensionContext;

/**
 * Container extensions allows you to extend the basic functionality of containers.
 * <p>
 * Subclasses of this class must give open rights to app.packed.base
 * <p>
 * Extensions form the basis, extensible model
 * 
 * <p>
 * Subclasses of this class that are actively used should be final.
 */

// Step1
// final Extension
// package private constructor
// open to app.packed.base
// exported to other users to use

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

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker SW = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    static {
        SharedSecrets._initialize(new AppPackedExtensionAccess() {

            @Override
            public void buildBundle(Extension extension, Builder builder) {
                extension.buildDescriptor(builder);
            }

            /** {@inheritDoc} */
            @Override
            public void initializeExtension(PackedExtensionContext context) {
                context.extension.context = context;
                context.extension.onAdded();
            }

            /** {@inheritDoc} */
            @Override
            public void onConfigured(Extension extension) {
                extension.onConfigured();
            }

            @Override
            public void onPrepareContainerInstantiation(Extension extension, ArtifactInstantiationContext context) {
                extension.onPrepareContainerInstantiation(context);
            }

            @Override
            public <E extends Extension, T extends ExtensionPipeline<T>> T wireletNewPipeline(E extension, ExtensionWirelet<E, T> wirelet) {
                return wirelet.newPipeline(extension);
            }

            @Override
            public <E extends Extension, T extends ExtensionPipeline<T>> void wireletProcess(T pipeline, ExtensionWirelet<E, T> wirelet) {
                wirelet.process(pipeline);
            }
        });
    }

    /** The extension context. This field should never be read directly, but only accessed via {@link #context()}. */
    private PackedExtensionContext context;

    /**
     * Returns the build context of the artifact which this extension is a part of.
     * <p>
     * Im thinking about throwing ISE on instantiation....
     * 
     * @throws IllegalStateException
     *             if invoked from constructor or {@link #onPrepareContainerInstantiation(ArtifactInstantiationContext)}.
     * @return the build context
     */
    protected final ArtifactBuildContext buildContext() {
        // The thing i'm worried about here is wirelets...
        // Because this should not be used on instantiation time because
        // Any wirelets specified when initializing an image is not included...
        // Or is this controllable from ContainerConfiguration????
        ArtifactBuildContext c = context().buildContext();
        checkConfigurable();
        return c;
    }

    protected void buildDescriptor(BundleDescriptor.Builder builder) {}

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on {@link Extension} or a subclass of it .
     * <p>
     * Invoking this method typically takes in the order of 1-2 microseconds.
     * <p>
     * If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
     * {@link ConfigSite#UNKNOWN}.
     * 
     * @param operation
     *            the operation
     * @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
     * @see StackWalker
     */
    // TODO add stuff about we also ignore non-concrete container sources...
    protected final ConfigSite captureStackFrame(String operation) {
        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = SW.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? context().containerConfigSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    final boolean captureStackFrameIgnoreFilter(StackFrame f) {
        Class<?> c = f.getDeclaringClass();
        return Extension.class.isAssignableFrom(c)
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && ContainerSource.class.isAssignableFrom(c));
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after the extensions {@link #onConfigured()} has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    protected final void checkConfigurable() {
        context().checkConfigurable();
    }

    /**
     * Returns this extension's context. Or fails with {@link IllegalStateException} if invoked from the constructor of the
     * extension.
     * 
     * @return the configuration of the container
     */
    private PackedExtensionContext context() {
        // When calling this method remember to add test to BasicExtensionTest
        PackedExtensionContext c = context;
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
     * This callback method is invoked (by the runtime) immediately after this extension has been instantiated and added to
     * the configuration of the container, but before the extension instance has been returned to the user. This method is
     * typically invoked as the result of a user calling {@link ContainerConfiguration#use(Class)}.
     * <p>
     * The newly instantiated extension is returned to the user immediately after this method returns.
     * <p>
     * Unless any errors occur, {@link #onConfigured()} is the next callback method that is invoked by the runtime.
     * <p>
     * The default implementation of this method does nothing.
     */
    protected void onAdded() {}

    /**
     * A callback method that is invoked immediately after a container has been successfully configured. This is typically
     * after {@link Bundle#configure()} has returned.
     * <p>
     * <p>
     * The default implementation of this method does nothing.
     */
    // If the container contains multiple extensions. They are invoked in reverse order. If E2 has a dependency on E1.
    // E2.onConfigured() will be invoked before E1.onConfigure(). This is done in order to allow extensions to perform
    // additional configuration on other extension after user code has been executed
    protected void onConfigured() {} // afterConfigure,

    /**
     * Invoked whenever the container is being instantiated. In case of a container image this means that method might be
     * invoked multiple times. Even by multiple threads
     * 
     * @param context
     *            an instantiation context object
     */
    // Maa koeres efter trae ting??? Eller ogsaa skal det foregaa paa trae tingen...
    // Nope det skal ikke foregaa paa trae tingen. Fordi den skal kun bruges hvis man ikke
    // har extension communication
    protected void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {}

    protected final void putIntoInstantiationContext(ArtifactInstantiationContext context, Object sidecar) {
        context().putIntoInstantiationContext(context, sidecar);
    }

    final void runWithLookup(Lookup lookup, Runnable runnable) {
        // Ideen er at vi kan installere component. o.s.v. med det specificeret lookup....
        // D.v.s. vi laver en push, pop af et evt. eksisterende lookup object
        // En install fra en extension skal jo naesten bruge denne..
        // Faktisk, er der lidt sikkerhedshullumhej her.... Hvordan sikre vi os at extensions.
        // Ikke goer noget sjovt her. Hmm, altsaa indvitere man en extension indenfor...

        // Men vi vel helst have at de giver adgang via module-info...
        // Eller via Factory.withLookup();
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * Invoking this method is similar to calling {@link ContainerConfiguration#use(Class)}. However, this method also keeps
     * track of which extensions uses other extensions. And forming any kind of circle in the dependency graph will fail
     * with a runtime exception.
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
        return context().use(extensionType);
    }

    /**
     * Returns a list of any wirelets that was used to configure the container.
     * <p>
     * Invoking this method is equivalent to invoking {@code configuration().wirelets()}.
     * 
     * @return a list of any wirelets that was used to configure the container
     */
    protected final WireletList wirelets() {
        return context().wirelets();
    }
}
