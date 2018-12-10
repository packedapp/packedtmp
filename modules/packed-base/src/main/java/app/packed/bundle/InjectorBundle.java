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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import app.packed.inject.ServiceConfiguration;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * A injector bundle provides a simple way to package services into a resuable container nice little thingy.
 * 
 * Bundles provide a simply way to package components and service. For example, so they can be used easily across
 * multiple containers. Or simply for organizing a complex project into distinct sections, such that each section
 * addresses a separate concern.
 * <p>
 * Bundle are useually
 *
 * <pre>
 * class WebServerBundle extends Bundle {
 *
 *     private port = 8080; //default port
 *
 *     protected void configure() {
 *        install(new WebServer(port));
 *     }
 *
 *     public WebServerBundle setPort(int port) {
 *         checkNotFrozen();
 *         this.port = port;
 *         return this;
 *     }
 * }
 * </pre>
 *
 * The bundle is used like this:
 *
 * <pre>
 * ContainerBuilder b = new ContainerBuilder();
 * b.use(WebServiceBundle.class).setPort(8080);
 *
 * Container c = cc.newContainer();
 * </pre>
 * <p>
 * Bundles must have a single public no argument constructor.
 * <p>
 * Bundles have no direct dependency on the JPMS (Java Platform Module System). However, typically a module exposes only
 * a single extension.
 * <p>
 * Bundles are strictly a configuration and initialization time concept. Bundles are not available
 *
 * Bundles are automatically added as dependencies. And can be dependency injected into components while the
 * <p>
 * Since container configurations are reusable. For example, the following code is perfectly legal:
 *
 * <pre>
 * ContainerConfiguration cc = new ContainerConfiguration();
 * Container c1 = cc.setName("C1").create();
 * Container c2 = cc.setName("C2").create();
 *
 * </pre>
 *
 * An extension contains two main method for modifying a containers configuration. The first one is using which is
 * invoked exactly once. And that is when the extension is added to containers configuration.
 *
 * The second method is which is invoked every time a new container is instantiated.
 *
 *
 *
 * For simplicity reasons there are no version mechanism for extensions. Handling different versions of the same
 * extension must be done by the user.
 *
 */
// Take text about extensions from here htt://junit.org/junit5/docs/current/user-guide/#extensions
// Deactive extension as defenied in 5.3 http://junit.org/junit5/docs/current/user-guide/#extensions
// See stuff here https://github.com/Netflix/governator/wiki/Module-Dependencies

// Rename to Bundle??? Problem with extension is that when you create a modular application
// You do not think of your modules as extension

// Dependencies via Bundle Constructor. All classes exported from the bundle are available for injection into the module
// Also we can use @Optional to declare optional extensions...
// Do we auto install extensions???????? Or do we just automatically install them if needed

// If we did the same thing as Guice with AbstractModule + Module
// We would have a public outerfacing configure(Binder) which would be annoying
// Also see below....

// @ApiNote this class is an abstract class to avoid situations where a malicious person where ome
// YOU CAN NEVER PASSE ARE BUNDLECONFIGURATOR DIRECTLY TO BUNDLE and HAVE IT FILLED OUT, as we do not want
// MethodHAndls.Lookup leaking!!!!!!!!
// We could make a protected method that people can override and make public if they want.
// Also for testing it would be really usefull configure(Binder)
// Or maybe

// ID256 BundleHash????? API wise. SpecHash..
public abstract class InjectorBundle extends Bundle {

    /** The internal configuration to delegate to */
    // We probably want to null this out...
    // If we install the bundle as a component....
    // We do not not want any more garbage then needed.
    private InjectorBuilder builder;

    protected final <T> T asDeprecated(T t, String reason) {
        return t;
    }

    protected final <T> T asPreview(T t, String reason) {
        return t;
    }

    /**
     * @param builder
     *            the injector configuration to delagate to
     * @param freeze
     * @apiNote we take an AbstractBundleConfigurator instead of a BundleConfigurator to make sure we never parse an
     *          external configurator by accident. And we some let the bundle implementation invoke
     *          {@link #lookup(java.lang.invoke.MethodHandles.Lookup)} on a random interface. Thereby letting the Lookup
     *          object escape.
     */
    final void configure(InjectorBuilder builder, boolean freeze) {

        // Maybe we can do some access checkes on the Configurator. To allow for testing....

        if (this.builder != null) {
            throw new IllegalStateException();
        } else if (isFrozen && freeze) {
            // vi skal have love til f.eks. at koere en gang descriptor af, saa det er kun hvis vi skal freeze den ogsaa doer.
            throw new IllegalStateException("Cannot configure this bundle, after it has been been frozen");
        }
        this.builder = requireNonNull(builder);
        try {
            configure();
        } finally {
            this.builder = null;
            if (freeze) {
                isFrozen = true;
            }
        }
    }

    final void ifPropertySet(String value, Runnable r) {
        // SetThreadLocal
        ifPropertySet("foo", () -> {
            bind("Foo");
        });
        // ClearThreadLocal
    }

    @Override
    InjectorBuilder configuration() {
        if (builder == null) {
            throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to call Bundle.configure directly");
        }
        return builder;
    }

    // Ideen er at man man extend et Bundle, med et nyt bundle der har test information
    // Lav nogle testvaerktoejer der aabner dem istedet syntes jeg
    // Eller saetter et pre-filter ind...
    protected final void overwrite(ServiceConfiguration<?> sc) {

    }

    // requireAll();
    // require(Predicate<? super Dependenc> p); //require(e->!e.isOptional);
}
