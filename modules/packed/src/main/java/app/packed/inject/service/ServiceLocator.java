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
import java.util.function.Consumer;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationMirror;
import app.packed.base.Key;
import app.packed.base.Reflectable;
import app.packed.base.TypeToken;
import app.packed.container.Assembly;
import app.packed.container.ComposerAction;
import app.packed.container.Wirelet;
import app.packed.hooks.accessors.ScopedProvide;
import app.packed.inject.Provider;
import app.packed.inject.variable.BeanDependencyHook;
import packed.internal.inject.service.build.PackedServiceComposer;
import packed.internal.inject.service.runtime.PackedInjector;

/**
 * Extends {@link ServiceRegistry} with method for acquiring service instances.
 * <p>
 * Unless otherwise specified service locators are always immutable.
 */
@BeanDependencyHook(extension = ServiceExtension.class)
public interface ServiceLocator extends ServiceRegistry {

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
        requireNonNull(action, "action is null");
        Optional<T> o = findInstance(key);
        if (o.isPresent()) {
            T instance = o.get();
            action.accept(instance);
        }
    }

    /**
     * Returns a service selection with all of the services in this locator.
     * 
     * @return a service selection with all of the services in this locator
     */
    ServiceSelection<?> selectAll();

    /**
     * Returns a service selection where the raw type of every service key is assignable to the specified type.
     * <p>
     * Unlike this method {@link #selectWithAnyQualifiers(Class)} this method will also select any
     * 
     * @param <T>
     *            the assignable type
     * @param type
     *            the assignable type
     * @return the service selection
     */
    <T> ServiceSelection<T> selectAssignableTo(Class<T> type);

    // Maaske drop withAnyQualifiers
    default <T> ServiceSelection<T> selectWithAnyQualifiers(Class<T> typePart) {
        return selectWithAnyQualifiers(TypeToken.of(typePart));
    }

    /**
     * @param <T>
     *            the service type
     * @param typePart
     *            the type part of the key
     * @return
     */
    <T> ServiceSelection<T> selectWithAnyQualifiers(TypeToken<T> typePart);

    /**
     * Spawns a new service locator by using a {@link ServiceComposer} to transmute this locator.
     * <p>
     * INSERT EXAMPLE
     * 
     * <p>
     * If you
     * 
     * @param action
     *            the transmutation action
     * @return the new service locator
     */
    @Reflectable
    ServiceLocator spawn(ComposerAction<ServiceComposer> action);

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
    static ApplicationDriver<ServiceLocator> driver() {
        throw new UnsupportedOperationException();
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
    static ApplicationImage<ServiceLocator> imageOf(Assembly assembly, Wirelet... wirelets) {
        return driver().imageOf(assembly, wirelets);
    }

    // maaske har vi launcher og Image...
    @Reflectable
    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return driver().mirrorOf(assembly, wirelets);
    }

    /** {@return a service locator that provide no services} */
    static ServiceLocator of() {
        return PackedInjector.EMPTY_SERVICE_LOCATOR;
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

    /**
     * Creates a new service locator via a service composer.
     * 
     * @param action
     *            the composition action
     * @return a new service locator
     * @see #driver()
     */
    @Reflectable
    static ServiceLocator of(ComposerAction<? super ServiceComposer> action) {
        return PackedServiceComposer.of(action);
    }

    @ScopedProvide
    private static ServiceLocator provide() {
        // I think we need to mark somewhere that we need to create a ServiceLocator
        return ServiceLocator.of();
    }

    @Reflectable
    static ApplicationImage<ServiceLocator> reusableImageOf(Assembly assembly, Wirelet... wirelets) {
        return driver().reusableImageOf(assembly, wirelets);
    }
}

interface ServiceLocatorZandbox extends ServiceLocator {

    // Ideen er lidt at vi tager alle keys. Hvor man kan fjerne 0..n qualififiers
    // og saa faa den specificeret key.

    // Kunne godt taenke mig at finde et godt navn.x
    // Naar en noegle er en super noegle???

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

    // Vi har faktisk 3.
    // Key Delen = Foo.class; (Ignores qualifiers)
    // Key delend.rawType = Foo.class
    // Key delen er assignable. <--- ved ikke hvor tit man skal bruge den

    // All whose raw type is equal to.. Don't know if it is
    default <T> ServiceSelection<T> selectRawType(Class<T> serviceType) {
        throw new UnsupportedOperationException();
    }
}
