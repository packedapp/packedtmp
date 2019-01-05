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

import java.util.Set;

import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Qualifier;
import app.packed.util.TypeLiteral;
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
    // private InjectorBuilder injectorBuilder;

    /**
     * Binds the specified implementation as a new service. The runtime will use {@link Factory#findInjectable(Class)} to
     * find a valid constructor or method to instantiate the service instance once the injector is created.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the {@code Class} is annotated with
     * a {@link Qualifier qualifier annotation}, the default key will have the qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param implementation
     *            the implementation to bind
     * @return a service configuration for the service
     * @see InjectorConfiguration#bind(Class)
     */
    protected final <T> ServiceConfiguration<T> bind(Class<T> implementation) {
        return injectorBuilder().bind(implementation);
    }

    protected final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        return injectorBuilder().bind(factory);
    }

    protected final <T> ServiceConfiguration<T> bind(T instance) {
        return injectorBuilder().bind(instance);
    }

    protected final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        return injectorBuilder().bind(implementation);
    }

    protected final void bindInjector(Class<? extends InjectorBundle> bundleType, BundlingStage... filters) {
        injectorBuilder().bindInjector(bundleType, filters);
    }

    /**
     * Imports the services that are available in the specified injector.
     *
     * @param injector
     *            the injector to import services from
     * @param stages
     *            any number of filters that restricts the services that are imported. Or makes them available under
     *            different keys
     * @see InjectorConfiguration#bindInjector(Injector, BundlingStage...)
     * @throws IllegalArgumentException
     *             if the specified stages are not instance all instance of {@link BundlingImportStage} or combinations (via
     *             {@link BundlingStage#andThen(BundlingStage)} thereof
     */
    protected final void bindInjector(Injector injector, BundlingStage... stages) {
        injectorBuilder().bindInjector(injector, stages);
    }

    protected final void bindInjector(InjectorBundle bundle, BundlingStage... stages) {
        injectorBuilder().bindInjector(bundle, stages);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        return injectorBuilder().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        return injectorBuilder().bindLazy(factory);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        return injectorBuilder().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        return injectorBuilder().bindPrototype(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        return injectorBuilder().bindPrototype(factory);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        return injectorBuilder().bindPrototype(implementation);
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
        //
        // if (this.injectorBuilder != null) {
        // throw new IllegalStateException();
        // } else if (isFrozen && freeze) {
        // // vi skal have love til f.eks. at koere en gang descriptor af, saa det er kun hvis vi skal freeze den ogsaa doer.
        // throw new IllegalStateException("Cannot configure this bundle, after it has been been frozen");
        // }
        // this.injectorBuilder = requireNonNull(builder);
        // try {
        // configure();
        // } finally {
        // this.injectorBuilder = null;
        // if (freeze) {
        // isFrozen = true;
        // }
        // }
        throw new UnsupportedOperationException();
    }

    InjectorBuilder injectorBuilder() {
        return support().withs(InjectorBuilder.class);
        // if (injectorBuilder == null) {
        // throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to
        // call Bundle.configure directly");
        // }
        // return injectorBuilder;
    }

    /**
     * Exposes an internal service outside of this bundle, equivalent to calling {@code expose(Key.of(key))}. A typical use
     * case if having a single
     * 
     * When you expose an internal service, the descriptions and tags it may have are copied to the exposed services.
     * Overridden them will not effect the internal service from which the exposed service was created.
     * 
     * <p>
     * Once an internal service has been exposed, the internal service is made immutable. For example,
     * {@code setDescription()} will fail in the following example with a runtime exception: <pre>{@code 
     * ServiceConfiguration<?> sc = bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);
     * sc.setDescription("foo");}
     * </pre>
     * <p>
     * A single internal service can be exposed under multiple keys: <pre>{@code 
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service1.class).setDescription("Service 1");
     * expose(ServiceImpl.class).as(Service2.class).setDescription("Service 2");}
     * </pre>
     * 
     * @param <T>
     *            the type of the exposed service
     * 
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #expose(Key)
     */
    protected final <T> ServiceConfiguration<T> expose(Class<T> key) {
        return injectorBuilder().expose(key);
    }

    /**
     * Exposes an internal service outside of this bundle.
     * 
     * 
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);}
     * </pre>
     * 
     * @param <T>
     *            the type of the exposed service
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #expose(Key)
     */
    protected final <T> ServiceConfiguration<T> expose(Key<T> key) {
        return injectorBuilder().expose(key);
    }

    protected final <T> ServiceConfiguration<T> expose(ServiceConfiguration<T> configuration) {
        return injectorBuilder().expose(configuration);
    }

    protected void requireService(Key<?> key) {
        injectorBuilder().requireService(key);
    }

    /**
     * Sets the description of the injector or container.
     * 
     * @param description
     *            the description of the injector or container
     * @see InjectorConfiguration#setDescription(String)
     * @see Injector#getDescription()
     */
    protected final void setDescription(@Nullable String description) {
        injectorBuilder().setDescription(description);
    }

    protected final Set<String> tags() {
        return injectorBuilder().tags();
    }

    protected void requireService(Class<?> key) {
        injectorBuilder().requireService(key);
    }
}
