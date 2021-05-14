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

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.base.Qualifier;
import app.packed.component.ComponentDriver;
import app.packed.inject.Factory;
import app.packed.inject.Provide;
import app.packed.inject.ServiceBeanConfiguration;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import app.packed.state.sandbox.OnStart;
import packed.internal.inject.service.sandbox.InjectorComposer;

/**
 * Extends {@link ContainerAssembly} with functionality from some of the commonly used extensions available.
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
// Skal have en strategi for hvilke extension vi har med
// og hvilke metoder fra disse extensions vi har med
// TODO tror vi sortere metoderne efter extension og saa efter navn
public abstract class BaseAssembly extends ContainerAssembly {

    /** Creates a new assembly using {@link ContainerConfiguration#driver()}. */
    protected BaseAssembly() {}

    /**
     * Creates a new assembly using the specified driver.
     * 
     * @param driver
     *            the container driver to use
     */
    protected BaseAssembly(ComponentDriver<ContainerConfiguration> driver) {
        super(driver);
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

    protected final void main(Runnable runnable) {
        throw new UnsupportedOperationException();
    }
    
    // Provides a result...
    protected final void main(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    protected final void exportAll() {
        service().exportAll();
    }

    /**
     * Returns whether or not the specified extension is in use.
     * 
     * @param extensionClass
     *            the extension class to test
     * @return whether or not the specified extension is in use
     * @throws IllegalArgumentException
     *             if the specified extension type is {@link Extension}
     */
    protected final boolean isInUse(Class<? extends Extension> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        if (extensionClass == Extension.class) {
            throw new IllegalArgumentException("Cannot specify Extension.class");
        }
        throw new UnsupportedOperationException();
        // return container.extensions.keySet().contains(extensionClass);
    }

//    /**
//     * Returns a lifecycle extension instance, installing it if it has not already been installed.
//     * 
//     * @return a lifecycle extension instance
//     */
//    protected final EntryPointExtension lifecycle() {
//        return use(EntryPointExtension.class);
//    }

    /**
     * Binds the specified implementation as a new service. The runtime will use {@link Factory#of(Class)} to find a valid
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
     * @see InjectorComposer#provide(Class)
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
     *            the type of component to install
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

    protected final void requireGuest() {
        // requirePassive <--- maaske er den her i virkeligheden meget mere interessant...

        // Vi skal have en eller anden maade at kunne specificere det her

    }
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
