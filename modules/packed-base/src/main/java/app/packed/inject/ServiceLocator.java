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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.TypeToken;
import packed.internal.inject.service.runtime.PackedInjector;

/**
 * A service locator is an immutable collection of services that allows for accessing services instances.
 */
// Auto activating... Hvis man har den som parameter
public interface ServiceLocator extends ServiceRegistry {

    /**
     * Returns a service instance for the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service exists for the specified key, use {@link #use(Class)} for more fluent code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key for which to return a service instance
     * @return an optional containing the service instance if present, or an empty optional if not present
     * @see #use(Class)
     */
    default <T> Optional<T> findInstance(Class<T> key) {
        return findInstance(Key.of(key));
    }

    /**
     * Returns a service instance for the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service exists for the specified key, use {@link #use(Class)} for more fluent code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key for which to return a service instance
     * @return an optional containing the service instance if present, or an empty optional if not present
     * @see #use(Class)
     */
    <T> Optional<T> findInstance(Key<T> key);

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
        Optional<T> t = findInstance(key);
        requireNonNull(action, "action is null");
        if (t.isPresent()) {
            T tt = t.get();
            action.accept(tt);
        }
    }

    // Vi har faktisk 3.
    // Key Delen = Foo.class; (Ignores qualifiers)
    // Key delend.rawType = Foo.class
    // Key delen er assignable. <--- ved ikke hvor tit man skal bruge den

    // may define any qualifiers
    default <T> ServiceSelection<T> select(Class<T> keyRawKeyType) {
        // select(Number.class) will select @Named("foo") Number but not Integer
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a service selection with all of the services in this locator with a {@link Key} whose {@link Key#rawType()}
     * is {@link Class#isAssignableFrom(Class) assignable} to the specified type.
     * <p>
     * Primitive types will automatically be boxed if specified.
     * 
     * @return a service selection with all of the services in this locator with a key whose raw type is assignable to the
     *         specified service type
     * @see Class#isAssignableFrom(Class)
     * @see Key#rawType()
     */
    // Hmm kan vi sige noget om actual type som vi producere???
    default <T> ServiceSelection<T> select(TypeToken<T> key) {
        // May define additional qualifiers
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a service selection with all of the services in this locator.
     * 
     * @return a service selection with all of the services in this locator
     */
    default ServiceSelection<Object> selectAll() {
        throw new UnsupportedOperationException();
    }

    // All whose raw type is equal to.. Don't know if it is
    default <T> ServiceSelection<T> selectRawType(Class<T> serviceType) {
        throw new UnsupportedOperationException();
    }

    // All whose raw type can be assigned to
    default <T> ServiceSelection<T> selectAssignableTo(Class<T> serviceType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new service locator by transformation the service that this locator provides.
     * 
     * @param transformer
     *            the transformer
     * @return the new service locator
     */
    ServiceLocator transform(Consumer<ServiceTransformer> transformer);

    /**
     * Returns a service of the specified type. Or throws a {@link NoSuchElementException} if this injector does not provide
     * a service with the specified key. The semantics method is identical to {@link #findInstance(Class)} except that an
     * exception is thrown instead of returning if the service does not exist.
     *
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service for the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     * @see #contains(Class)
     */
    default <T> T use(Class<T> key) {
        return use(Key.of(key));
    }

    /**
     * Returns a service with the specified type, or throws a {@link NoSuchElementException} if no such service exists. This
     * is typically used to create fluent APIs such as:
     *
     * <pre>{@code
     * Key<WebServer> key = Key.of(WebServer.class);
     * registry.use(WebServer.class).printAllLiveConnections();}
     * </pre>
     *
     * Invoking this method is equivalent to:
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
     *            the type of service this method returns
     * @param key
     *            the key of the service to return
     * @return a service with the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     */
    default <T> T use(Key<T> key) {
        Optional<T> t = findInstance(key);
        if (!t.isPresent()) {
            throw new NoSuchElementException("A service with the specified key could not be found, key = " + key);
        }
        return t.get();
    }

    /**
     * Returns an empty service locator.
     * 
     * @return an empty service locator
     */
    static ServiceLocator of() {
        return PackedInjector.EMPTY_SERVICE_LOCATOR;
    }
}
// toRegistry...
// or ServiceRegistry.copyOf(); Not a copy.. a view where you cannot access the instance.

//* @throws IllegalStateException
//*             if a service with the specified key exist, but the service is not ready to be consumed yet. For example,
//*             if injecting an injector into a constructor of a service and then using the injector to try and access
//*             other service that have not been properly initialized yet. For example, a service that depends on the
//*             service being constructed