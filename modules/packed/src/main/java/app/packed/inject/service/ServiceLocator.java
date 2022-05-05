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
        throw new UnsupportedOperationException();
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
