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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.container.Container;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.inject.Qualifier;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.TypeLiteral;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * Bundles provide a simply way to package components and service. This is useful, for example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link InjectorBundle}</b> which bundles information about services and creates {@link Injector} instances
 * using {@link Injector#of(Class)}.</li>
 * <li><b>{@link ContainerBundle}</b> which bundles information about both services and creates {@link Container}
 * instances using {@link Container#of(Class)}.</li>
 * </ul>
 */
public abstract class Bundle {

    static {
        BundleSupport.Helper.init(new BundleSupport.Helper() {

            /** {@inheritDoc} */
            @Override
            protected void configureInjectorBundle(InjectorBundle bundle, InjectorBuilder configuration, boolean freeze) {
                bundle.configure(configuration, freeze);
            }

            /** {@inheritDoc} */
            @Override
            protected void importExportStageOnFinish(ImportExportStage stage) {
                stage.onFinish();
            }
        });
    }

    /** Whether or not {@link #configure()} has been invoked. */
    boolean isFrozen;

    /** Prevent users from extending this class. */
    Bundle() {}

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
        return configuration().bind(implementation);
    }

    protected final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        return configuration().bind(factory);
    }

    protected final <T> ServiceConfiguration<T> bind(T instance) {
        return configuration().bind(instance);
    }

    protected final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        return configuration().bind(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        return configuration().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        return configuration().bindLazy(factory);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        return configuration().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        return configuration().bindPrototype(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        return configuration().bindPrototype(factory);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        return configuration().bindPrototype(implementation);
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
    protected final void checkNotFrozen() {
        if (isFrozen) {
            throw new IllegalStateException("The configuration of this bundle has been frozen");
        }
    }

    /**
     * Returns the configuration object that we delegate to.
     * 
     * @return the configuration object that we delegate to
     */
    abstract InjectorBuilder configuration();

    /** Configures the bundle using the various protected methods. */
    protected abstract void configure();

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
        return configuration().expose(key);
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
        return configuration().expose(key);
    }

    protected final <T> ServiceConfiguration<T> expose(ServiceConfiguration<T> configuration) {
        return configuration().expose(configuration);
    }

    protected final void injectorBind(Class<? extends InjectorBundle> bundleType, ImportExportStage... filters) {
        configuration().injectorBind(bundleType, filters);
    }

    /**
     * Imports the services that are available in the specified injector.
     *
     * @param injector
     *            the injector to import services from
     * @param filters
     *            any number of filters that restricts the services that are imported. Or makes them available under
     *            different keys
     * @see InjectorConfiguration#injectorBind(Injector, InjectorImportStage...)
     */
    protected final void injectorBind(Injector injector, InjectorImportStage... filters) {
        configuration().injectorBind(injector, filters);
    }

    protected final void injectorBind(InjectorBundle bundle, ImportExportStage... filters) {
        configuration().injectorBind(bundle, filters);
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
        configuration().lookup(lookup);
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
        configuration().setDescription(description);
    }

    protected final Set<String> tags() {
        return configuration().tags();
    }

    protected void requireMandatory(Class<?> key) {
        configuration().requireMandatory(key);
    }

    protected void requireMandatory(Key<?> key) {
        configuration().requireMandatory(key);
    }

}
