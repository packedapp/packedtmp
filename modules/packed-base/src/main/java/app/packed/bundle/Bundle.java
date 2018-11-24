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
import app.packed.inject.AbstractInjectorStage;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.InjectorImportStage;
import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.TypeLiteral;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;
import packed.internal.inject.buildnodes.InternalInjectorConfiguration;

/**
 * Bundles provide a simply way to package components and service. For example, so they can be used easily across
 * multiple containers. Or simply for organizing a complex project into distinct sections, such that each section
 * addresses a separate concern.
 * <p>
 * There are currently two types of bundles available in Packed:
 * 
 * <ul>
 * <li><b>{@link InjectorBundle}</b> which bundles information about services and from which {@link Injector injectors}
 * can be created.</li>
 * <li><b>{@link ContainerBundle}</b> which bundles information about both services and components and from which
 * {@link Container containers} can be created.</li>
 * </ul>
 */
public abstract class Bundle {

    static {
        BundleSupport.Helper.init(new BundleSupport.Helper() {

            @Override
            protected void configure(InjectorBundle bundle, InternalInjectorConfiguration delegate, boolean freeze) {
                bundle.configure(delegate, freeze);
            }
        });
    }

    /** Whether or not {@link #configure()} has been invoked. */
    boolean isFrozen;
    Bundle() {}

    protected final <T> ServiceConfiguration<T> bind(Class<T> implementation) {
        return internal().bind(implementation);
    }

    protected final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        return internal().bind(factory);
    }

    protected final <T> ServiceConfiguration<T> bind(T instance) {
        return internal().bind(instance);
    }

    protected final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        return internal().bind(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        return internal().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        return internal().bindLazy(factory);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        return internal().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        return internal().bindPrototype(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        return internal().bindPrototype(factory);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        return internal().bindPrototype(implementation);
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
     * Configures the bundle.
     *
     * This method is invoked after the user has invoked but before the container has been instantiated. Since the user has
     * fully populated the configuration, this method can be used to install agents, dependencies and services that depends
     * on the contents of the configuration. This is done via invoking methods on ContainerInitializer and <b>NOT</b> on the
     * container configuration using }.
     * <p>
     * If multiple extensions have been added to the container. This method will be invoked in the order they have been
     * added.
     */
    // Can we have bundles without configuration???, maybe just 5 Provides method????
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
        return internal().expose(key);
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
        return internal().expose(key);
    }

    protected final <T> ServiceConfiguration<T> expose(ServiceConfiguration<T> configuration) {
        throw new UnsupportedOperationException();
        // return internal().expose(configuration);
    }

    public void foo() {}

    protected final void injectorBind(Class<? extends InjectorBundle> bundleType, AbstractInjectorStage... filters) {
        internal().injectorBind(bundleType, filters);
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
        internal().injectorBind(injector, filters);
    }

    protected final void injectorBind(InjectorBundle bundle, AbstractInjectorStage... filters) {
        internal().injectorBind(bundle, filters);
    }

    abstract InternalInjectorConfiguration internal();

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     */
    protected final void lookup(Lookup lookup) {
        internal().lookup(lookup);
    }

    /**
     * Sets the description of the injector or container.
     * 
     * @param description
     *            the description of the injector or container
     */
    protected final void setDescription(@Nullable String description) {
        internal().setDescription(description);
    }

    protected final Set<String> tags() {
        return internal().tags();
    }

}
