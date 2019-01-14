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

import app.packed.container.ComponentConfiguration;
import app.packed.container.Container;
import app.packed.hook.Hook;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorBundle;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.container.ContainerBuilder;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * Bundles provide a simply way to package components and service and build modular application. This is useful, for
 * example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link InjectorBundle}</b> which bundles information about services, and creates {@link Injector} instances
 * using {@link Injector#of(Class)}.</li>
 * <li><b>{@link Bundle}</b> which bundles information about both services and components, and creates {@link Container}
 * instances using {@link Container#of(Class)}.</li>
 * </ul>
 */

// Descriptor does not freeze, Injector+Container freezes
public abstract class Bundle {

    /** Whether or not {@link #configure()} has been invoked. */
    boolean isFrozen;

    BundleConfigurationContext context;

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
     * Returns the bundle support object which
     * 
     * @return the bundle support object
     */
    protected final BundleConfigurationContext context() {
        // Vi laver en bundle nyt per configuration.....
        BundleConfigurationContext s = context;
        if (s == null) {
            throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to call Bundle.configure directly");
        }
        return s;
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     * @see InjectorConfiguration#lookup(Lookup)
     */
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        context().lookup(lookup);
    }

    /**
     * Opens the bundle for modification later on
     */
    protected final void open() {
        // Nope....
    }

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
     * a {@link Hook qualifier annotation}, the default key will have the qualifier annotation added.
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

    protected final void wireInjector(Class<? extends InjectorBundle> bundleType, WiringOperation... operations) {
        injectorBuilder().wireInjector(bundleType, operations);
    }

    /**
     * Imports the services that are available in the specified injector.
     *
     * @param injector
     *            the injector to import services from
     * @param stages
     *            any number of filters that restricts the services that are imported. Or makes them available under
     *            different keys
     * @see InjectorConfiguration#wireInjector(Injector, WiringOperation...)
     * @throws IllegalArgumentException
     *             if the specified stages are not instance all instance of {@link UpstreamWiringOperation} or combinations
     *             (via {@link WiringOperation#andThen(WiringOperation)} thereof
     */
    protected final void wireInjector(Injector injector, WiringOperation... operations) {
        injectorBuilder().wireInjector(injector, operations);
    }

    protected final void wireInjector(InjectorBundle bundle, WiringOperation... operations) {
        injectorBuilder().wireInjector(bundle, operations);
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
        return context().with(InjectorBuilder.class);
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
    protected final <T> ServiceConfiguration<T> export(Key<T> key) {
        return injectorBuilder().expose(key);
    }

    protected final <T> ServiceConfiguration<T> exportService(Class<T> key) {
        return injectorBuilder().expose(key);
    }

    protected final <T> ServiceConfiguration<T> export(ServiceConfiguration<T> configuration) {
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

    ContainerBuilder containerBuilderX() {
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

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this bundle will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link ComponentConfiguration} can be used to specify a specific parent.
     *
     * @param <T>
     *            the type of component to install
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> ComponentConfiguration<T> install(T instance) {
        return containerBuilderX().install(instance);
    }

    protected final <T> ComponentConfiguration<T> install(TypeLiteral<T> implementation) {
        return containerBuilderX().install(implementation);
    }

    protected final void wire(Class<? extends Bundle> bundleType, WiringOperation... stages) {
        containerBuilderX().wireContainer(bundleType, stages);
    }

    protected final void wire(Bundle bundle, WiringOperation... stages) {
        containerBuilderX().wireContainer(bundle, stages);
    }

    protected final void wireContainer(Class<? extends Bundle> bundleType, WiringOperation... stages) {
        containerBuilderX().wireContainer(bundleType, stages);
    }

    protected final void wireContainer(Bundle bundle, WiringOperation... stages) {
        containerBuilderX().wireContainer(bundle, stages);
    }
}

// protected void lookup(Lookup lookup, LookupAccessController accessController) {}
// protected final void checkNotNativeRuntime() {
// if (GraalSupport.inImageRuntimeCode()) {
// StackFrame f = StackWalker.getInstance().walk(e -> e.skip(1).findFirst().get());
// throw new IllegalStateException("Cannot call " + f.getMethodName() + "() when running as a native-image");
// }
// }