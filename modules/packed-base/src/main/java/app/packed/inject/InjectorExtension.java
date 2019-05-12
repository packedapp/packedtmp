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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.WiringOption;
import app.packed.container.ComponentServiceConfiguration;
import app.packed.container.Extension;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Qualifier;
import app.packed.util.TypeLiteral;
import packed.internal.inject.builder.ContainerBuilder;
import packed.internal.inject.builder.ProvideAll;

/**
 * An extension used with injection.
 */
// manualRequirementManagement(); Do we need or can we just say that we should extend this contract exactly?
public final class InjectorExtension extends Extension<InjectorExtension> {

    /**
     * Adds the specified key to the list of optional services.
     * 
     * @param key
     *            the key to add
     */
    public void addOptional(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        builder().services().addOptional(key);
    }

    /**
     * Adds the specified key to the list of required services.
     * 
     * @param key
     *            the key to add
     */
    public void addRequired(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        builder().services().addRequired(key);
    }

    public void autoRequire() {
        builder().serviceAutoRequire();
    }

    private ContainerBuilder builder() {
        return (ContainerBuilder) super.configuration;
    }

    public <T> ServiceConfiguration<T> export(Class<T> key) {
        requireNonNull(key, "key is null");
        return export(Key.of(key));
    }

    /**
     * Exposes an internal service outside of this bundle.
     * 
     * 
     * <pre>
     *  {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * 
     * <pre>
     *  {@code  
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
    public <T> ServiceConfiguration<T> export(Key<T> key) {
        return builder().export(key);
    }

    public <T> ServiceConfiguration<T> export(ServiceConfiguration<T> configuration) {
        return builder().export(configuration);
    }

    public <T> ComponentServiceConfiguration<T> installService(Class<T> implementation) {
        return builder().installService(Factory.findInjectable(implementation));
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
    public <T> ComponentServiceConfiguration<T> installService(Factory<T> factory) {
        return builder().installService(factory);
    }

    public <T> ComponentServiceConfiguration<T> installService(TypeLiteral<T> implementation) {
        return builder().installService(Factory.findInjectable(implementation));
    }
    // ServicesDescriptor descriptor (extends Contract????) <- What we got so far....

    public <T> ServiceConfiguration<T> provide(Class<T> implementation) {
        return provide(Factory.findInjectable(implementation));
    }

    public <T> ServiceConfiguration<T> provide(Factory<T> factory) {
        return builder().provide(factory);
    }

    /**
     * Binds the specified instance as a new service.
     * <p>
     * The default key for the service will be {@code instance.getClass()}. If the type returned by
     * {@code instance.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will have the
     * qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param instance
     *            the instance to bind
     * @return a service configuration for the service
     */
    public <T> ServiceConfiguration<T> provide(T instance) {
        requireNonNull(instance, "instance");
        return builder().provide(instance);
    }

    public <T> ServiceConfiguration<T> provide(TypeLiteral<T> implementation) {
        return provide(Factory.findInjectable(implementation));
    }

    // Services are the default implementation of injection....

    // Export

    // Outer.. checker configurable, node. finish den sidste o.s.v.
    // Saa kalder vi addNode(inner.foo);

    // export

    // And then wrap it in ComponentServiceConfiguration....
    // void ServiceConfiguration<?> provide(ComponentConfiguration configuration);

    public void provideAll(Injector injector, WiringOption... operations) {
        ProvideAll pa = new ProvideAll(builder(), injector, operations);// Validates arguments
        checkConfigurable();
        builder().freezeLatest();
        pa.process();
    }
}
