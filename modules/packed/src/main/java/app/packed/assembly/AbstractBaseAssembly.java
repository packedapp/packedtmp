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
package app.packed.assembly;

import java.util.Optional;

import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.service.ProvidableBeanConfiguration;

/**
 * Extends {@link BuildableAssembly} with shortcuts for commonly used methods on {@link BaseExtension} and
 * {@link ContainerConfiguration}.
 * <p>
 * For example, instead of calling {@code use(BaseExtension.class).provide(FooBean.class)} you can just call
 * {@code provide(FooBean.class)}.
 */
public abstract class AbstractBaseAssembly extends BuildableAssembly {

    /**
     * Returns a {@link BaseExtension} instance.
     * <p>
     * Calling this method is short for {@code use(BaseExtension.class)}
     *
     * @return a base extension instance
     * @see #use(Class)
     */
    protected final BaseExtension base() {
        return use(BaseExtension.class);
    }

    /**
     * Returns the configuration of the root container defined by this assembly.
     * <p>
     * This method can only be called from within the {@link #build()} method. Trying to call it outside of {@link #build()}
     * will throw an {@link IllegalStateException}.
     *
     * @return the configuration of the container
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
    protected final ContainerConfiguration container() {
        return assembly().assembly.container.configuration();
    }

    protected final void exportAll() {
        base().exportAll();
    }

    /**
     * Installs a component that will use the specified {@link Op} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     *
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     * @see BaseExtension#install(Class)
     */
    // add? i virkeligheden wire vi jo class komponenten...
    // Og taenker, vi har noget a.la. configuration().wire(ClassComponent.Default.bind(implementation))
    protected final <T> ProvidableBeanConfiguration<T> install(Class<T> implementation) {
        return base().install(implementation);
    }

    /**
     * Installs a bean that will use the specified {@link Op} to instantiate the bean.
     *
     * @param <T>
     *            the type of bean to install
     * @param op
     *            the operation using for instantiating the bean
     * @return the configuration of the bean
     * @see BaseAssembly#install(Op)
     */
    protected final <T> ProvidableBeanConfiguration<T> install(Op<T> op) {
        return base().install(op);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this container will have have component as its
     * parent.
     *
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> ProvidableBeanConfiguration<T> installInstance(T instance) {
        return base().installInstance(instance);
    }

    /**
     * Links the specified assembly as part of the same application and container that this container is part of.
     *
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a mirror of the container that was linked
     */
    // Maybe have a link  <- that takes the simple name of assembly
    protected final void link(String name, Assembly assembly, Wirelet... wirelets) {
        base().link(name, assembly, wirelets);
    }

    protected final void link(Assembly assembly, Wirelet... wirelets) {
        base().link(assembly.getClass().getSimpleName().replace("Assembly", ""), assembly, wirelets);
    }

    /**
     * Sets the name of this assembly's container. The name must consists only of alphanumeric characters and '_', '-' or
     * '.'. The name is case sensitive.
     * <p>
     * This method should be called as the first thing when configuring this assembly.
     * <p>
     * If no name is set using this method. The framework will automatically assign a name to the container, in such a way
     * that it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the container
     * @see ContainerConfiguration#named(String)
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @throws IllegalStateException
     *             if called from outside {@link #build()}
     */
    protected final void named(String name) {
        container().named(name);
    }

    /**
     * Provides a service by instantiating a single instance of the specified class.
     * <p>
     * This method is shortcut for ....
     * <p>
     * The runtime will use {@link Op#factoryOf(Class)} to find a valid constructor or method to instantiate the service
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
     */
    protected final <T> ProvidableBeanConfiguration<T> provide(Class<T> implementation) {
        ProvidableBeanConfiguration<T> configuration = base().install(implementation);
        return configuration.provide();
    }

    /**
     *
     *
     * @param <T>
     *            the type of bean to provide as a service
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    protected final <T> ProvidableBeanConfiguration<T> provide(Op<T> factory) {
        ProvidableBeanConfiguration<T> configuration = base().install(factory);
        return configuration.provide();
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
    protected final <T> ProvidableBeanConfiguration<T> provideInstance(T instance) {
        return installInstance(instance).provide();
    }

    protected final <T> ProvidableBeanConfiguration<T> providePrototype(Class<T> implementation) {
        return base().installPrototype(implementation).provide();
    }

    protected final <T> ProvidableBeanConfiguration<T> providePrototype(Op<T> factory) {
        return base().installPrototype(factory).provide();
    }

    protected final <T extends Wirelet> Optional<T> selectWirelet(Class<T> wireletClass) {
        return selectWirelets(wireletClass).last();
    }

    /**
     * Selects all wirelets that available and {@link Class#isAssignableFrom(Class) assignable} to the specified wirelet
     * class.
     *
     * @param <W>
     *            the type of wirelets to select
     * @param wireletClass
     *            the type of wirelets to select
     * @return a wirelet selection
     * @see ContainerConfiguration#selectWirelets(Class)
     */
    protected final <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return container().selectWirelets(wireletClass);
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is the first time an extension of the specified type has been requested. This method will create a new
     * instance of the extension. This instance will then be returned for all subsequent requests for the same extension
     * type.
     *
     * @param <E>
     *            the type of extension to return
     * @param extensionClass
     *            the Class object corresponding to the extension type
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the assembly's container is no longer configurable and the specified type of extension has not been
     *             used previously
     * @implNote this method delegates all calls to ContainerConfiguration#use(Class)
     */
    protected final <E extends Extension<E>> E use(Class<E> extensionClass) {
        return container().use(extensionClass);
    }
}

//protected final void requireGuest() {
////requirePassive <--- maaske er den her i virkeligheden meget mere interessant...
//
////Vi skal have en eller anden maade at kunne specificere det her
//
//}
//
///**
//* Returns a {@link ScheduledJobExtension} instance.
//* <p>
//* Calling this method is short for {@code use(SchedulerExtension.class)}
//*
//* @return a time extension instance
//* @see #use(Class)
//*/
//protected final ScheduledJobExtension scheduler() {
//return use(ScheduledJobExtension.class);
//}

///**
//* Returns a {@link TimeExtension} instance.
//* <p>
//* Calling this method is short for {@code use(TimeExtension.class)}
//*
//* @return a time extension instance
//* @see #use(Class)
//*/
//protected final TimeExtension time() {
//  return use(TimeExtension.class);
//}
//I don't think they are used that often...
//protected final void require(Class<?> key) {
//service().require(Key.of(key));
//}
//
//protected final void require(Key<?>... keys) {
//service().require(keys);
//}
//
//protected final void requireOptionally(Class<?> key) {
//service().requireOptionally(Key.of(key));
//}
//
//protected final void requireOptionally(Key<?>... keys) {
//service().requireOptionally(keys);
//}

///**
//* Exposes an internal service outside of this container, equivalent to calling {@code expose(Key.of(key))}. A typical
//* use case if having a single
//*
//* When you expose an internal service, the descriptions and tags it may have are copied to the exposed services.
//* Overridden them will not effect the internal service from which the exposed service was created.
//*
//* <p>
//* Once an internal service has been exposed, the internal service is made immutable. For example,
//* {@code setDescription()} will fail in the following example with a runtime exception: <pre>{@code
//* ServiceConfiguration<?> sc = bind(ServiceImpl.class);
//* expose(ServiceImpl.class).as(Service.class);
//* sc.setDescription("foo");}
//* </pre>
//* <p>
//* A single internal service can be exposed under multiple keys: <pre>{@code
//* bind(ServiceImpl.class);
//* expose(ServiceImpl.class).as(Service1.class).setDescription("Service 1");
//* expose(ServiceImpl.class).as(Service2.class).setDescription("Service 2");}
//* </pre>
//*
//* @param <T>
//*            the type of the exposed service
//*
//* @param key
//*            the key of the internal service to expose
//* @return a service configuration for the exposed service
//* @see #export(Key)
//*/
//protected final <T> ExportedServiceConfiguration<T> export(Class<T> key) {
//  return service().export(key);
//}
//
///**
//* Exposes an internal service outside of this container.
//*
//*
//* <pre> {@code
//* bind(ServiceImpl.class);
//* expose(ServiceImpl.class);}
//* </pre>
//*
//* You can also choose to expose a service under a different key then what it is known as internally in the
//* <pre> {@code
//* bind(ServiceImpl.class);
//* expose(ServiceImpl.class).as(Service.class);}
//* </pre>
//*
//* @param <T>
//*            the type of the exposed service
//* @param key
//*            the key of the internal service to expose
//* @return a service configuration for the exposed service
//* @see #export(Key)
//*/
//protected final <T> ExportedServiceConfiguration<T> export(Key<T> key) {
//  return service().export(key);
//}
///**
//* Returns whether or not the specified extension is in use.
//*
//* @param extensionType
//*            the extension class to test
//* @return whether or not the specified extension is in use
//* @throws IllegalArgumentException
//*             if the specified extension type is {@link Extension}
//*/
//protected final boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
// return container().isExtensionUsed(extensionType);
//}
