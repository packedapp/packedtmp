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
package app.packed.inject.service;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.base.Key;
import app.packed.base.Reflectable;
import app.packed.bean.BeanExtension;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.inject.Provider;
import app.packed.operation.dependency.DependencyProvider;
import packed.internal.inject.service.PackedServiceLocator;

/**
 * 
 * <p>
 * Unless otherwise specified the set of services provided by a service locator is always unchangeable.
 */
@DependencyProvider.Hook(extension = BeanExtension.class)
public interface ServiceLocator {

    /**
     * Returns {@code true} if this registry contains a service with the specified key.
     *
     * @param key
     *            key whose presence in this registry is to be tested
     * @return {@code true} if a service with the specified key is present in this registry. Otherwise {@code false}
     * @see #contains(Key)
     */
    default boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }
    
    /**
     * Returns {@code true} if this registry contains a service with the specified key.
     *
     * @param key
     *            key whose presence in this registry is to be tested
     * @return {@code true} if a service with the specified key is present in this registry. Otherwise {@code false}
     * @see #contains(Class)
     */
    default boolean contains(Key<?> key) {
        requireNonNull(key, "key is null");
        return keys().contains(key);
    }

    /**
     * Returns a service instance for the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service exists for the specified key, {@link #use(Class)} usually gives more fluent
     * code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key of the service to find
     * @return an optional containing the service instance if present, or an empty optional if not present
     * @see #use(Class)
     */
    default <T> Optional<T> findInstance(Class<T> key) {
        return findInstance(Key.of(key));
    }

    /**
     * Returns a service instance for the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service exists for the specified key, {@link #use(Class)} usually gives more fluent
     * code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key of the service to find
     * @return an optional containing the service instance if present, or an empty optional if not present
     * @see #use(Key)
     */
    default <T> Optional<T> findInstance(Key<T> key) {
        return findProvider(key).map(p -> p.provide());
    }
    
    default <T> Optional<Provider<T>> findProvider(Class<T> key) {
        return findProvider(Key.of(key));
    }

    <T> Optional<Provider<T>> findProvider(Key<T> key);

    /**
     * If a service with the specified key is present, performs the given action with a service instance, otherwise does
     * nothing.
     *
     * @param key
     *            the key to test
     * @param action
     *            the action to be performed, if a service with the specified key is present
     */
    default <T> void ifPresent(Class<T> key, Consumer<? super T> action) {
        ifPresent(Key.of(key), action);
    }

    /**
     * If a service with the specified key is present, performs the given action with a service instance, otherwise does
     * nothing.
     *
     * @param key
     *            the key to test
     * @param action
     *            the action to be performed, if a service with the specified key is present
     */
    default <T> void ifPresent(Key<T> key, Consumer<? super T> action) {
        requireNonNull(action, "action is null");
        Optional<T> o = findInstance(key);
        if (o.isPresent()) {
            T instance = o.get();
            action.accept(instance);
        }
    }

    /** {@return true if this registry contains any services, otherwise false} */
    default boolean isEmpty() {
        return keys().isEmpty();
    }
    

    /**
     * Returns a set view containing the keys for every service in this registry.
     * <p>
     * If this registry supports removals, the returned set will also support removal operations: {@link Set#clear()},
     * {@link Set#remove(Object)}, {@link Set#removeAll(java.util.Collection)},
     * {@link Set#removeIf(java.util.function.Predicate)} and {@link Set#retainAll(java.util.Collection)}. or via any set
     * iterators. The returned map will never support insertion or update operations.
     * <p>
     * The returned map will retain any thread-safety guarantees provided by the registry itself.
     * 
     * @return a set view containing the keys for every service in this registry
     */
    Set<Key<?>> keys();

    /** { @return the number of services in this locator} */
    default int size() {
        return keys().size();
    }

    /**
     * Returns a service with the specified key. Or throws a {@link NoSuchElementException} if no such service is available.
     * <p>
     * The semantics of this method are identical to {@link #findInstance(Class)} except that an exception is thrown instead
     * of returning if the service does not exist.
     *
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service for the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     * @see #find(Class)
     */
    default <T> T use(Class<T> key) {
        return use(Key.of(key));
    }

    /**
     * Returns a service with the specified type, or throws a {@link NoSuchElementException} if no such service exists. This
     * method is typically used to create fluent APIs such as:
     *
     * <pre>{@code
     * Key<WebServer> key = Key.of(WebServer.class);
     * locator.use(key).printAllLiveConnections();}
     * </pre>
     *
     * The default implementation of this method does:
     *
     * <pre>{@code
     *  Optional<T> t = find(key);
     *  if (!t.isPresent()) {
     *      throw new NoSuchElementException();
     *  }
     *  return t.get();}
     * </pre>
     *
     * @param <T>
     *            the type of service instance this method returns
     * @param key
     *            the key of the service instance to return
     * @return a service instance for the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     */
    default <T> T use(Key<T> key) {
        Optional<T> t = findInstance(key);
        if (!t.isPresent()) {
            throw new NoSuchElementException("A service with the specified key does not exist, key = " + key);
        }
        return t.get();
    }

    /**
     * Returns an application driver that can be used to create standalone service locator instances.
     * 
     * @return an application driver
     * @see #imageOf(Assembly, Wirelet...)
     * @see #of(Consumer)
     * @see #of(Assembly, Wirelet...)
     */
    private static ApplicationDriver<ServiceLocator> driver() {
        throw new UnsupportedOperationException();
    }
    

    // maaske har vi launcher og Image...
    @Reflectable
    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return driver().mirrorOf(assembly, wirelets);
    }

    
    /**
     * Creates a new service locator image from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     * @see #driver()
     */
    @Reflectable
    static ApplicationLauncher<ServiceLocator> newLauncher(Assembly assembly, Wirelet... wirelets) {
        return driver().imageOf(assembly, wirelets);
    }

    @Reflectable
    static ApplicationLauncher<ServiceLocator> newReusableLauncher(Assembly assembly, Wirelet... wirelets) {
        return driver().reusableImageOf(assembly, wirelets);
    }

    /** {@return a service locator that provides no services.} */
    static ServiceLocator of() {
        return new PackedServiceLocator(Map.of());
    }

    /**
     * Creates a new standalone service locator from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly that should be used to build the service locator
     * @param wirelets
     *            optional wirelets
     * @return a new service locator
     * @see #driver()
     */
    @Reflectable
    static ServiceLocator of(Assembly assembly, Wirelet... wirelets) {
        return driver().launch(assembly, wirelets);
    }
}
/**
 * An injector is an immutable holder of services that can be dependency injected or looked up by their type at runtime.
 * An injector is typically created by populating an injector builder with the various services that needs to be
 * available.
 *
 * These
 *
 * _ xxxxx Injector controls the services that are available from every container at runtime and is typically used for
 * and for injection.
 *
 * Typically a number of injectors exist. The container injector is.
 *
 *
 *
 * Container injector can be.
 *
 * For example, the injector for a component will also include a Component service. Because an instance of the component
 * interface can always be injected into any method.
 *
 *
 * <p>
 * An Injector instance is usually acquired in one of the following three ways:
 * <h3>Directly from a Container instance</h3> By calling container.getService(ServiceManager.class)}:
 *
 * <pre>
 * Container c = ...;
 * Injector injector = c.getService(Injector.class);
 * System.out.println(&quot;Available services: &quot; + Injector.services());
 * </pre>
 *
 * <h3>Annotated method, such as OnStart or OnStop</h3> When using annotations such as OnStart or OnStop. An injected
 * component manager can be used to determine which parameters are available for injection into the annotated method.
 *
 * <pre>
 * &#064;RunOnStart()
 * public void onStart(ServiceManager ServiceManager) {
 *     System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 * }
 * </pre>
 *
 * <h3>Injecting it into a Constructor</h3> Or, by declaring it as a parameter in the constructor of a service or agent
 * registered using container builder or container builder
 *
 * <pre>
 * public class MyService {
 *     public MyService(ServiceManager ServiceManager) {
 *         System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 *     }
 * }
 * </pre>
 *
 * <p>
 * The map returned by this method may vary doing the life cycle of a container. For example, if this method is invoked
 * in the constructor of a service registered with container builder. An instance of container builder is present in the
 * map returned. However, after the container has been initialized, the container will no longer keep a reference to the
 * configuration instance. So instances of Injector will never be available from any service manager after the container
 * has fully started.
 * <p>
 * Injectors are always immutable, however, extensions of this interface might provide mutable operations for methods
 * unrelated to injection.
 */
// Description... hmm its just super helpful...
// Injector does not have a name. In many cases there are a container behind an Injector.
// But if, for example, a component has its own injector. That injector does not have a container behind it.

// Do we have an internal injector and an external injector?????
// Or maybe an Injector and an InternalInjector (which if exportAll is the same???)

// Altsaa den hoerer vel ikke til her...
// Vi kan jo injecte andre ting en services

// Injector taenker jeg er component versionen...
// ServiceRegistry er service versionen...

// Aahhhh vi mangler nu end 4. version... ind imellem Injector og ServiceRegistry...

// Noget der kan injecte ting... Men ikke har en system component... 
