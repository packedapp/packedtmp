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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.app.App;
import app.packed.bundle.x.AppLaunch;
import app.packed.bundle.x.WiringOperation;
import app.packed.container.ComponentConfiguration;
import app.packed.container.Container;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Qualifier;
import app.packed.util.TypeLiteral;
import packed.internal.container.ContainerBuilder;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * Bundles provide a simply way to package components and build modular application. This is useful, for example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link Bundle}</b> which bundles information about services, and creates {@link Injector} instances using
 * .</li>
 * <li><b>{@link Bundle}</b> which bundles information about both services and components, and creates {@link Container}
 * instances using .</li>
 * </ul>
 */

// Descriptor does not freeze, Injector+Container freezes

// explicitServiceRequirements(); <- You can put it in an environment to force it

public abstract class Bundle {

    ContainerBuildContext context;

    /** Whether or not {@link #configure()} has been invoked. */
    boolean isFrozen;

    protected final Restrictions restrictions = null;

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
     * @see InjectorConfigurator#provide(Class)
     */
    // Rename to Provide@
    protected final <T> ServiceConfiguration<T> provide(Class<T> implementation) {
        return injectorBuilder().provide(implementation);
    }

    protected final void serviceAutoRequire() {
        injectorBuilder().serviceAutoRequire();
    }

    protected final <T> ServiceConfiguration<T> provide(Factory<T> factory) {
        return injectorBuilder().provide(factory);
    }

    protected final <T> ServiceConfiguration<T> provide(T instance) {
        return injectorBuilder().provide(instance);
    }

    protected final <T> ServiceConfiguration<T> provide(TypeLiteral<T> implementation) {
        return injectorBuilder().provide(implementation);
    }

    protected final <T> ServiceConfiguration<T> provideLazy(Class<T> implementation) {
        return injectorBuilder().provideLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> provideLazy(Factory<T> factory) {
        return injectorBuilder().provideLazy(factory);
    }

    protected final <T> ServiceConfiguration<T> provideLazy(TypeLiteral<T> implementation) {
        return injectorBuilder().provideLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> providePrototype(Class<T> implementation) {
        return injectorBuilder().providePrototype(implementation);
    }

    protected final <T> ServiceConfiguration<T> providePrototype(Factory<T> factory) {
        return injectorBuilder().providePrototype(factory);
    }

    // /** The internal configuration to delegate to */
    // We probably want to null this out...
    // If we install the bundle as a component....
    // We do not not want any more garbage then needed.
    // private InjectorBuilder injectorBuilder;

    protected final <T> ServiceConfiguration<T> providePrototype(TypeLiteral<T> implementation) {
        return injectorBuilder().providePrototype(implementation);
    }

    protected void buildWithBundle() {
        // Insta
        // NativeImageWriter
    }

    /**
     * Checks that the {@link #configure()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does try to configure the extension after it has been configured.
     *
     * <pre>{@code
     * public ManagementBundle setJMXEnabled(boolean enabled) {
     *     checkConfigurable(); //will throw IllegalStateException if configure() has already been called
     *     this.jmxEnabled = enabled;
     *     return this;
     * }}
     * </pre>
     * 
     * @throws IllegalStateException
     *             if the {@link #configure()} method has already been invoked once for this extension instance
     */
    protected final void checkConfigurable() {
        if (isFrozen) {
            // throw new IllegalStateException("This bundle is no longer configurable");
        }
    }

    /** Configures the bundle using the various methods from the inherited class. */
    protected abstract void configure();

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

    ContainerBuilder containerBuilderX() {
        return context().with(InjectorBuilder.class);
        // if (injectorBuilder == null) {
        // throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to
        // call Bundle.configure directly");
        // }
        // return injectorBuilder;
    }

    /**
     * Returns the bundle support object which
     * 
     * @return the bundle support object
     */
    protected final ContainerBuildContext context() {
        // Vi laver en bundle nyt per configuration.....
        ContainerBuildContext s = context;
        if (s == null) {
            throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to call Bundle.configure directly");
        }
        return s;
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
     * @see #export(Key)
     */
    protected final <T> ServiceConfiguration<T> export(Key<T> key) {
        return injectorBuilder().expose(key);
    }

    protected final <T> ServiceConfiguration<T> export(ServiceConfiguration<T> configuration) {
        return injectorBuilder().expose(configuration);
    }

    protected final <T> ServiceConfiguration<T> exportService(Class<T> key) {
        return injectorBuilder().expose(key);
    }

    protected final void exportHooks(Contract contract) {
        throw new UnsupportedOperationException();
    }

    protected final void exportHooks(Class<?>... hookTypes) {
        // interface = Instance Of Hooks
        throw new UnsupportedOperationException();
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
     * @see #export(Key)
     */
    protected final <T> ServiceConfiguration<T> export(Class<T> key) {
        return injectorBuilder().expose(key);
    }

    InjectorBuilder injectorBuilder() {
        return context().with(InjectorBuilder.class);
        // if (injectorBuilder == null) {
        // throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to
        // call Bundle.configure directly");
        // }
        // return injectorBuilder;
    }

    /**
     * Installs the specified component implementation. This method is short for
     * {@code install(Factory.findInjectable(implementation))} which basically finds a valid constructor/static method (as
     * outlined in {@link Factory#findInjectable(Class)}) to instantiate the specified component implementation.
     *
     * @param <T>
     *            the type of component to install
     * @param implementation
     *            the component implementation to install
     * @return a component configuration that can be use to configure the component in greater detail
     */
    protected final <T> ComponentConfiguration<T> install(Class<T> implementation) {
        return containerBuilderX().install(implementation);
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provides}.
     *
     * @param <T>
     *            the type of component to install
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    protected final <T> ComponentConfiguration<T> install(Factory<T> factory) {
        return containerBuilderX().install(factory);
    }

    protected final <T> ComponentConfiguration<T> install(TypeLiteral<T> implementation) {
        return containerBuilderX().install(implementation);
    }

    protected ComponentConfiguration<?> installBundle() {
        return containerBuilderX().install(this);
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     * @see InjectorConfigurator#lookup(Lookup)
     */
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        context().lookup(lookup);
    }

    protected final void lookup(Lookup lookup, Object lookupController) {
        // Ideen er at alle lookups skal godkendes at lookup controlleren...
        // Controller/Manager/LookupAccessManager
        // For module email, if you are paranoid.
        // You can specify a LookupAccessManager where every lookup access.
        // With both the source and the target. For example, service of type XX from Module YY in Bundle BB needs access to FFF
    }

    protected final Layer newEmptyLayer(String name, Layer... predecessors) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param name
     *            the name of the layer, must be unique among all layers defined in the same bundle
     * @param predecessors
     *            preds
     * @return the new layer
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, "main" or if another layer with the same name has been
     *             registered
     */
    protected final Layer newLayer(String name, Layer... predecessors) {
        // maybe "" is just the main layer...

        // Okay we need to able to addLayers as predessestors to the main layer....
        // sucessotorToMainLayer, precessorToMainLayer() only available for empty layer...

        // check same bundle
        // Maybe skip name and have a setter... If you put every bundle into its own layer it is a bit annoying...

        // The

        throw new UnsupportedOperationException();
    }

    protected final Layer mainLayer(Layer... predecessors) {
        // Can only be called once???
        throw new UnsupportedOperationException();
    }

    /**
     * Opens the bundle for modification later on
     */
    protected final void open() {
        // Nope....
    }

    protected void requireService(Class<?> key) {
        injectorBuilder().requireService(key);
    }

    protected void requireService(Key<?> key) {
        injectorBuilder().requireService(key);
    }

    /**
     * Sets the description of the injector or container.
     * 
     * @param description
     *            the description of the injector or container
     * @see InjectorConfigurator#setDescription(String)
     * @see Injector#description()
     */
    protected final void setDescription(@Nullable String description) {
        injectorBuilder().setDescription(description);
    }

    protected final Set<String> tags() {
        return injectorBuilder().tags();
    }

    // /**
    // * Install the specified component instance.
    // * <p>
    // * If this install operation is the first install operation of the container. The component will be installed as the
    // * root component of the container. All subsequent install operations on this bundle will have have component as its
    // * parent. If you wish to have a specific component as a parent, the various install methods on
    // * {@link ComponentConfiguration} can be used to specify a specific parent.
    // *
    // * @param <T>
    // * the type of component to install
    // * @param instance
    // * the component instance to install
    // * @return this configuration
    // */
    // protected final <T> ComponentConfiguration<T> install(T instance) {
    // return containerBuilderX().install(instance);
    // }

    protected final WiredBundle wire(Bundle child) {
        return new WiredBundle(this, requireNonNull(child, "child is null"));
    }

    // protected final void wire(Class<? extends Bundle> bundleType, WiringOperation... stages) {
    // containerBuilderX().wireContainer(bundleType, stages);
    // }

    protected final void wire(Bundle bundle, WiringOperation... stages) {
        containerBuilderX().wireContainer(bundle, stages);
    }

    // or have mainLayer() and then
    // newEmptyLayer(String name, includeMainAsSuccessor, BundleLayer... )
    // newEmptyLayer("Infrastructure", false);

    // Basically there are 4 types of new layer
    // empty
    // import from main layer
    // export to main layer
    // share with main layer

    // protected final void wireContainer(Bundle bundle, WiringOperation... stages) {
    // containerBuilderX().wireContainer(bundle, stages);
    // }

    // protected final void wireInjector(Bundle bundle, WiringOperation... operations) {
    // injectorBuilder().wireInjector(bundle, operations);
    // }
    //
    // /**
    // * Imports the services that are available in the specified injector.
    // *
    // * @param injector
    // * the injector to import services from
    // * @param stages
    // * any number of filters that restricts the services that are imported. Or makes them available under
    // * different keys
    // * @see InjectorConfiguration#wireInjector(Injector, WiringOperation...)
    // * @throws IllegalArgumentException
    // * if the specified stages are not instance all instance of {@link UpstreamWiringOperation} or combinations
    // * (via {@link WiringOperation#andThen(WiringOperation)} thereof
    // */
    // protected final void wireInjector(Injector injector, WiringOperation... operations) {
    // injectorBuilder().wireInjector(injector, operations);
    // }

    protected static void printDescriptor(Bundle bundle) {
        BundleDescriptor.of(bundle).print();
    }

    static protected void run(Bundle b, String[] args, WiringOperation... operations) {
        AppLaunch.of(b, args, operations).run();
    }

    static protected void run(Bundle b, WiringOperation... operations) {
        AppLaunch.of(b, operations).run();
    }

    static protected void run(Bundle b, Consumer<App> consumer, WiringOperation... operations) {
        AppLaunch.of(b, operations).run();
    }

    public interface Restrictions {
        void service(Class<?> clazz);
    }
}
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
 * Bundles are strictly a configuration and initialization time concept. Bundles are not available
 */

// ID256 BundleHash????? API wise. SpecHash..

// protected void lookup(Lookup lookup, LookupAccessController accessController) {}
// protected final void checkNotNativeRuntime() {
// if (GraalSupport.inImageRuntimeCode()) {
// StackFrame f = StackWalker.getInstance().walk(e -> e.skip(1).findFirst().get());
// throw new IllegalStateException("Cannot call " + f.getMethodName() + "() when running as a native-image");
// }
// }