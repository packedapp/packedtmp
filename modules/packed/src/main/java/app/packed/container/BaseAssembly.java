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
import app.packed.base.Qualifier;
import app.packed.bean.BeanExtension;
import app.packed.bean.ContainerBeanConfiguration;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import app.packed.inject.service.Provide;
import app.packed.inject.service.ServiceBeanConfiguration;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceLocator;
import app.packed.lifecycle.OnStart;

/**
 * Extends {@link Assembly} with shortcuts for some commonly used extensions.
 * <p>
 * For example, instead of doing use(ServiceExtension.class).provide(Foo.class) you can just use
 * service().provide(Foo.class) or even just provide(Foo.class).
 * <p>
 * All extensions defined in this module
 * 
 * time() TimeExtension
 * <p>
 * 
 * 
 * With common functionality provide by app.packed.base
 * 
 * <p>
 * 
 * Assemblies provide a simply way to package components and build modular application. This is useful, for example,
 * for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of assemblies available:
 * <ul>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about services, and creates injector instances using
 * .</li>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about both services and components, and creates
 * container instances using .</li>
 * </ul>
 * 
 * @apiNote We never return, for example, Assembly or BaseAssembly. As this would make extending the class difficult
 *          unless we defined all methods as non-final. Method Chaining is used on the component level... Not on the
 *          assembly level?
 */

/**
 * A container assembly. Typically you
 * 
 * 
 * Assemblies are the main source of system configuration. Basically a assembly is just a thin wrapper around
 * {@link BaseContainerConfiguration}. Delegating every invocation in the class to an instance of
 * {@link BaseContainerConfiguration} available via {@link #configuration()}.
 * <p>
 * A assembly instance can be used ({@link #build()}) exactly once. Attempting to use it multiple times will fail with
 * an {@link IllegalStateException}.
 * 
 * A generic assembly. Normally you would extend {@link BaseAssembly}
 * 
 * @see BaseAssembly
 */
// Skal have en strategi for hvilke extension vi har med
// og hvilke metoder fra disse extensions vi har med
// TODO tror vi sortere metoderne efter extension og saa efter navn
public abstract class BaseAssembly extends Assembly {

    /**
     * Returns a {@link BeanExtension} instance.
     * <p>
     * Calling this method is short for {@code use(BeanExtension.class)}
     * 
     * @return a bean extension instance
     * @see #use(Class)
     */
    protected final BeanExtension bean() {
        return use(BeanExtension.class);
    }

    /**
     * Returns a {@link ContainerExtension} instance.
     * <p>
     * Calling this method is short for {@code use(ContainerExtension.class)}
     * 
     * @return a container extension instance
     * @see #use(Class)
     */
    protected final ContainerExtension container() {
        return use(ContainerExtension.class);
    }

    /**
     * Exposes an internal service outside of this container, equivalent to calling {@code expose(Key.of(key))}. A typical
     * use case if having a single
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
    protected final <T> ExportedServiceConfiguration<T> export(Class<T> key) {
        return service().export(key);
    }

    /**
     * Exposes an internal service outside of this container.
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
    protected final <T> ExportedServiceConfiguration<T> export(Key<T> key) {
        return service().export(key);
    }

    protected final void exportAll() {
        service().exportAll();
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     * @see BeanExtension#install(Class)
     */
    // add? i virkeligheden wire vi jo class komponenten...
    // Og taenker, vi har noget a.la. configuration().wire(ClassComponent.Default.bind(implementation))
    protected final <T> ContainerBeanConfiguration<T> install(Class<T> implementation) {
        return bean().install(implementation);
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see BaseAssembly#install(Factory)
     */
    protected final <T> ContainerBeanConfiguration<T> install(Factory<T> factory) {
        return bean().install(factory);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this container will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link ServiceBeanConfiguration} can be used to specify a specific parent.
     *
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> ContainerBeanConfiguration<T> installInstance(T instance) {
        return bean().installInstance(instance);
    }

    /**
     * Returns whether or not the specified extension is in use.
     * 
     * @param extensionType
     *            the extension class to test
     * @return whether or not the specified extension is in use
     * @throws IllegalArgumentException
     *             if the specified extension type is {@link Extension}
     */
    protected final boolean isExtensionUsed(Class<? extends Extension> extensionType) {
        return configuration().isExtensionUsed(extensionType);
    }

    /**
     * Links the specified assembly as part of the same application and container that this container is part of.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a mirror of the container that was linked
     * @see ContainerConfiguration#link(Assembly, Wirelet...)
     */
    // Why not wire + wirelets???
    protected final ContainerMirror link(Assembly assembly, Wirelet... wirelets) {
        return configuration().link(assembly, wirelets);
    }

    protected final ContainerConfiguration link(ContainerDriver driver, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    protected final ContainerConfiguration link(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /**
     * Provides a service by instantiating a single instance of the specified class.
     * <p>
     * This method is shortcut for ....
     * <p>
     * The runtime will use {@link Factory#of(Class)} to find a valid constructor or method to instantiate the service
     * instance once the injector is created.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the {@code Class} is annotated with
     * a {@link Qualifier qualifier annotation}, the default key will have the qualifier annotation added.
     *
     * @param <T>
     *            the type of the service
     * @param implementation
     *            the bean implementation that should be instantiated and provided as a service
     * @return a configuration object for the service bean
     * @see ServiceExtension#provide(Class)
     */
    protected final <T> ServiceBeanConfiguration<T> provide(Class<T> implementation) {
        return service().provide(implementation);
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provide}.
     *
     * @param <T>
     *            the type of bean to provide as a service
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    protected final <T> ServiceBeanConfiguration<T> provide(Factory<T> factory) {
        return service().provide(factory);
    }

    protected final void provideAll(ServiceLocator locator) {
        service().provideAll(locator);
    }

    /**
     * Binds a new service constant to the specified instance.
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
    protected final <T> ServiceBeanConfiguration<T> provideInstance(T instance) {
        return service().provideInstance(instance);
    }

    protected final <T> ServiceBeanConfiguration<T> providePrototype(Class<T> implementation) {
        return service().providePrototype(implementation);
    }

    protected final <T> ServiceBeanConfiguration<T> providePrototype(Factory<T> factory) {
        return service().providePrototype(factory);
    }

//    protected final void requireGuest() {
//        // requirePassive <--- maaske er den her i virkeligheden meget mere interessant...
//
//        // Vi skal have en eller anden maade at kunne specificere det her
//
//    }
//
//    /**
//     * Returns a {@link ScheduledJobExtension} instance.
//     * <p>
//     * Calling this method is short for {@code use(SchedulerExtension.class)}
//     * 
//     * @return a time extension instance
//     * @see #use(Class)
//     */
//    protected final ScheduledJobExtension scheduler() {
//        return use(ScheduledJobExtension.class);
//    }

    /**
     * Returns a {@link ServiceExtension} instance.
     * <p>
     * Calling this method is short for {@code use(ServiceExtension.class)}
     * 
     * @return a service extension instance
     * @see #use(Class)
     */
    protected final ServiceExtension service() {
        return use(ServiceExtension.class);
    }

//    /**
//     * Returns a {@link TimeExtension} instance.
//     * <p>
//     * Calling this method is short for {@code use(TimeExtension.class)}
//     * 
//     * @return a time extension instance
//     * @see #use(Class)
//     */
//    protected final TimeExtension time() {
//        return use(TimeExtension.class);
//    }
}

// I don't think they are used that often...
//protected final void require(Class<?> key) {
//  service().require(Key.of(key));
//}
//
//protected final void require(Key<?>... keys) {
//  service().require(keys);
//}
//
//protected final void requireOptionally(Class<?> key) {
//  service().requireOptionally(Key.of(key));
//}
//
//protected final void requireOptionally(Key<?>... keys) {
//  service().requireOptionally(keys);
//}
