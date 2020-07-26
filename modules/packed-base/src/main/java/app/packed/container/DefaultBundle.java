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

import app.packed.base.Key;
import app.packed.base.Key.Qualifier;
import app.packed.component.SingletonConfiguration;
import app.packed.inject.Factory;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.lifecycleold.OnStart;
import app.packed.service.Injector;
import app.packed.service.InjectorAssembler;
import app.packed.service.Provide;
import app.packed.service.ServiceComponentConfiguration;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceExtension;

/**
 * A convenience extension of {@link ContainerBundle} which contains shortcut access to common functionality defined by
 * the various extension available in this module.
 * <p>
 * For example, instead of doing use(ServiceExtension.class).provide(Foo.class) you can just use
 * service().provide(Foo.class) or even just provide(Foo.class).
 * <p>
 * 
 * With common functionality provide by app.packed.base
 * 
 * <p>
 * 
 * Bundles provide a simply way to package components and build modular application. This is useful, for example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link DefaultBundle}</b> which bundles information about services, and creates {@link Injector} instances
 * using .</li>
 * <li><b>{@link DefaultBundle}</b> which bundles information about both services and components, and creates container
 * instances using .</li>
 * </ul>
 * 
 * 
 * @apiNote We never return, for example, Bundle or BaseBundle. As this would make extending the class difficult unless
 *          we defined all methods as non-final.
 */
public abstract class DefaultBundle extends ContainerBundle {

    /**
     * Returns a {@link BaseExtension} instance.
     * 
     * @return a base extension instance
     */
    protected final BaseExtension base() {
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
        return services().export(key);
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
        return services().export(key);
    }

    protected final <T> ServiceConfiguration<T> export(ServiceComponentConfiguration<T> configuration) {
        return services().export(configuration);
    }

//    /**
//     * Returns a lifecycle extension instance, installing it if it has not already been installed.
//     * 
//     * @return a lifecycle extension instance
//     */
//    protected final EntryPointExtension lifecycle() {
//        return use(EntryPointExtension.class);
//    }

    protected final void exportAll() {
        services().exportAll();
    }

    protected final LifecycleExtension lifecycle() {
        return use(LifecycleExtension.class);
    }

    /**
     * Binds the specified implementation as a new service. The runtime will use {@link Factory#find(Class)} to find a valid
     * constructor or method to instantiate the service instance once the injector is created.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the {@code Class} is annotated with
     * a {@link Qualifier qualifier annotation}, the default key will have the qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param implementation
     *            the implementation to bind
     * @return a service configuration for the service
     * @see InjectorAssembler#provide(Class)
     */
    protected final <T> ServiceComponentConfiguration<T> provide(Class<T> implementation) {
        // Provide er i virkeligheden... install() followed by service().provide(Singleton or Static)
        return services().provide(implementation);
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provide}.
     *
     * @param <T>
     *            the type of component to install
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    protected final <T> ServiceComponentConfiguration<T> provide(Factory<T> factory) {
        return services().provide(factory);
    }

    protected final <T> ServiceComponentConfiguration<T> provide(SingletonConfiguration<T> configuration) {
        throw new UnsupportedOperationException();
    }

    protected final void provideAll(Injector injector, Wirelet... wirelets) {
        services().provideAll(injector, wirelets);
    }

    protected final <T> ServiceComponentConfiguration<T> provideConstant(T instance) {
        return services().provideConstant(instance);
    }

    protected final void require(Class<?> key) {
        services().require(Key.of(key));
    }

    protected final void require(Key<?>... keys) {
        services().require(keys);
    }

    protected final void requireOptionally(Class<?> key) {
        services().requireOptionally(Key.of(key));
    }

    protected final void requireOptionally(Key<?>... keys) {
        services().requireOptionally(keys);
    }

    /**
     * Returns a {@link ServiceExtension} instance.
     * 
     * @return a service extension instance
     */
    protected final ServiceExtension services() {
        return use(ServiceExtension.class);
    }

    /**
     * Prints the contract of the specified bundle.
     * 
     * @param bundle
     *            the bundle to print the contract for
     */
    protected static void printContract(ContainerBundle bundle) {
        // BaseBundleContract.of(bundle).print();
    }

    protected static void printDescriptor(ContainerBundle bundle) {
        BundleDescriptor.of(bundle).print();
    }
}