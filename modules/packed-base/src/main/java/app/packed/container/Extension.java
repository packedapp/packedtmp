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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Modifier;
import java.util.Optional;

import app.packed.config.ConfigSite;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.extension.AbstractExtensionModelBuilder;
import packed.internal.module.AppPackedExtensionAccess;
import packed.internal.module.ModuleAccess;

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

public abstract class Extension {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    static {
        ModuleAccess.initialize(AppPackedExtensionAccess.class, new AppPackedExtensionAccess() {

            /** {@inheritDoc} */
            @Override
            public void configureComposer(ExtensionComposer<?> composer, AbstractExtensionModelBuilder context) {
                composer.doConfigure(context);
            }

            /** {@inheritDoc} */
            @Override
            public void setExtensionContext(Extension extension, ExtensionContext context) {
                extension.context = context;
            }

            @Override
            public void pipelineInitialize(ExtensionWirelet.Pipeline<?, ?, ?> pipeline) {
                pipeline.onInitialize();
            }
        });
    }

    /** The extension context. This field should never be read directly, but only accessed via {@link #context()}. */
    private ExtensionContext context;

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements {@link ContainerSource}.
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
        // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
        // to the extension class in order to simplify the filtering mechanism.

        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? context().containerConfigSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    /**
     * @param frame
     *            the frame to filter
     * @return whether or not to filter the frame
     */
    private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {
        Class<?> c = frame.getDeclaringClass();
        // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
        // Syntes bare vi filtrer app.packed.base modulet fra...
        return Extension.class.isAssignableFrom(c)
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && ContainerSource.class.isAssignableFrom(c));
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * This method delegates to {@link ExtensionContext#checkConfigurable()}.
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
     * @throws IllegalArgumentException
     *             if invoked from the constructor of the extension
     * @return an extension context object
     */
    protected final ExtensionContext context() {
        ExtensionContext c = context;
        if (c == null) {
            throw new IllegalStateException("This operation cannot be invoked from the constructor of the extension."
                    + " As an alternative ExtensionComposer.onAdd(action) can used to perform initialization");
        }
        return c;
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
     *             If invoked from the constructor of the extension. Or if the underlying container is no longer
     *             configurable and an extension of the specified type has not already been installed
     * @throws UnsupportedOperationException
     *             if the specified extension type is not specified via {@link UseExtension} on this extension.
     */
    protected final <E extends Extension> E use(Class<E> extensionType) {
        return context().use(extensionType);
    }
}

//
// final void runWithLookup(Lookup lookup, Runnable runnable) {
// // Extensions bliver bare noedt til at vaere aabne for
//
// // Ideen er at vi kan installere component. o.s.v. med det specificeret lookup....
// // D.v.s. vi laver en push, pop af et evt. eksisterende lookup object
// // En install fra en extension skal jo naesten bruge denne..
// // Faktisk, er der lidt sikkerhedshullumhej her.... Hvordan sikre vi os at extensions.
// // Ikke goer noget sjovt her. Hmm, altsaa indvitere man en extension indenfor...
//
// // Men vi vel helst have at de giver adgang via module-info...
// // Eller via Factory.withLookup();
// }