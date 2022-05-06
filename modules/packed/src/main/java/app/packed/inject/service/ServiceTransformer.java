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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.Qualifier;
import app.packed.inject.Factory;

/**
 *
 *
 * @see ServiceWirelets#to(java.util.function.Consumer)
 * @see ServiceWirelets#from(java.util.function.Consumer)
 * @see PublicizeExtension#transformExports(java.util.function.Consumer)
 */

//Create services

//Create (Replaces existing services)
//-- Prototype (Factory, Class)
//-- Singleton (Factory, Class, Instance)
//-- Other ServiceLocator) 

//Update
//-- Rekey
//-- Reattribute
//-- Decorate
//-- Replace

//Delete
//-- Remove
//-- Retain

// Read
//-- Via ServiceRegistry

// Nested class paa ServiceWirelets if that is the only place it is going to be used
public interface ServiceTransformer {
 
   default ServiceContract contract() {
       throw new UnsupportedOperationException();
   }
    
    /**
     * A version of {@link #decorate(Key, Function)} that takes a {@code class} key. See other method for details.
     * 
     * @param <T>
     *            the type of the service that should be decorated
     * @param key
     *            the key of the service that should be decorated
     * @param decoratingFunction
     *            the decoration function
     * @throws NoSuchElementException
     *             if a service with the specified key does not exist
     * @see #decorate(Key, Function)
     */
    default <T> void decorate(Class<T> key, Function<? super T, ? extends T> decoratingFunction) {
        decorate(Key.of(key), decoratingFunction);
    }

    /**
     * Decorates a service with the specified key using the specified decoration function.
     * <p>
     * If the service that is being decorated is constant. The function will be invoked at most
     * 
     * @param <T>
     *            the type of the service that should be decorated
     * @param key
     *            the key of the service that should be decorated
     * @param decoratingFunction
     *            the decoration function
     * @throws NoSuchElementException
     *             if a service with the specified key does not exist
     * @see #decorate(Class, Function)
     */
    // TODO must check return type..
    <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction);

    /**
     * <p>
     * If the specified factory does not have declare any variables. The new services will have public (constant) scope.
     * 
     * @param factory
     * @throws IllegalStateException
     *             if the specified factory has dependencies that cannot be resolved among available services.
     */
    // Hvis psedokode eksempel
    // for every variable in factory {
    // if get(var.key).mode == prototype) {
    // return prototype(factory)
    // }
    // }
    // return provide(factory;
    //
    // Kunne gode bruge en factory.resolveAsKeys(); fail on context thingies??? I think

    // som provide med constant er styret af det der kommer ind...
    // in most situations you probably want to use this one

    void map(Factory<?> factory);
    
    default <T> void peek(Class<T> key, Consumer<? super T> consumer) {
        peek(Key.of(key), consumer);
    }
    
    <T> void peek(Key<T> key, Consumer<? super T> consumer);

    // provide a constant via an instance
    /**
     * Provides a new constant service returning the specified instance on every request.
     * 
     * @param <T>
     *            the type of the service being added
     * @param key
     *            the key of the service
     * @param instance
     *            the instance to return on every request
     * @see #provideInstance(Key, Object)
     * @see #provideInstance(Object)
     */
    default <T> void provideInstance(Class<T> key, T instance) {
        provideInstance(Key.of(key), instance);
    }

    /**
     * <p>
     * If an existing service with the specified key already exists this method will replace it.
     * 
     * @param <T>
     *            the type
     * @param key
     *            the key
     * @param instance
     *            the instance
     */
    <T> void provideInstance(Key<T> key, T instance);

    /**
     * Returns a wirelet that will provide the specified service to the target container. Iff the target container has a
     * service of the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code provideInstance(instance.getClass(), instance)}.
     * 
     * @param instance
     *            the service to provide
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default void provideInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        provideInstance((Class) instance.getClass(), instance);
    }

    // auto figure out if constant or prototype

    /**
     * A version of
     * 
     * @param existingKey
     *            the key of an existing service
     * @param newKey
     *            the new key of the service
     */
    // How useful is this. It is only for downcasting
    // FooImpl -> Foo
    default void rekey(Class<?> existingKey, Class<?> newKey) {
        requireNonNull(existingKey, "existingKey is null");
        requireNonNull(newKey, "newKey is null");
        rekey(Key.of(existingKey), Key.of(newKey));
    }

    /**
     * Changes the key of an existing service. This method is typically used to add or remove {@link Qualifier qualifiers}.
     * <p>
     * While adding and removing qualifiers is the main purpose of this method. Services can If changing the type of a
     * service it must be done in a compatible way.
     * <p>
     * Technically as services are immutable a new service is created.
     * 
     * @param existingKey
     *            the key of an existing service
     * @param newKey
     *            the new key of the service
     * @throws IllegalStateException
     *             if a service with newKey already exists
     * @throws ClassCastException
     *             if the raw type of new key is not assignable to the raw type of the service
     * @throws NoSuchElementException
     *             if a service with the specified key does not exist
     * @see #rekeyAll(Function)
     */
    // service(Key).asFoo()????
    // asFoo() laver jo en ny service....
    void rekey(Key<?> existingKey, Key<?> newKey); // Return the new service????

    /**
     * <p>
     * The rekeying function should not call other mutating operations on this transformer during computation.
     * 
     * <pre> {@code
     * for (Service s : this) {
     *    Key<?> key = rekeyingFunction.apply(s);
     *    if (key == null) {
     *       remove(key);
     *    } else if (!key.equals(s.key())) {
     *       rekey(s.key(), key);
     *    }
     * }
     * }</pre>
     * 
     * @param rekeyingFunction
     *            the function used for rekeying
     * @throws IllegalStateException
     *             if a service with newKey already exists
     * @throws ClassCastException
     *             if the type of any new key is not assignable to the service type
     */
    // Take text from Map#compute
    default void rekeyAll(Function<Key<?>, @Nullable Key<?>> rekeyingFunction) {
        for (Key<?> s : keys()) {
            Key<?> key = rekeyingFunction.apply(s);
            if (key == null) {
                remove(key);
            } else if (!key.equals(s)) {
                rekey(s, key);
            }
        }
    }

    /**
     * Rekey all service
     * 
     * @param tag
     */
    // IDK, can add them later
    // Navnet lyder lidt forkert. Vi tilfoejer et tag, mere
    // addkey, rekey
    // keyAdd, keyReplace?
    default void rekeyAllAddTag(String tag) {
        requireNonNull(tag, "tagis null");
        rekeyAll(s -> s.withTag(tag));
    }

    default void rekeyAllWith(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        rekeyAll(s -> s.with(qualifier));
    }

    default void rekeyAllWithClassTag(Class<?> tag) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Keys for which a corresponding service is not present, are ignored.
     * 
     * @param keys
     *            the keys that should be removed
     */
    default void remove(Class<?>... keys) {
        remove(Key.of(keys));
    }

    /**
     * Attempts to remove services with any of the specified keys.
     * <p>
     * Keys for which a service is not present are ignored.
     * 
     * @param keys
     *            the keys of services that should be removed
     */
    default void remove(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        for (Key<?> k : keys) {
            requireNonNull(k, "key in specified array is null");
            keys().remove(k);
        }
    }

    /** Removes all services. */
    void removeAll();

    /**
     * Remove every key {@link Class} or {@link Key}
     * 
     * @param keys
     *            the keys to remove
     * @throws IllegalArgumentException
     *             if the specified collection contain objects that are not instances of either {@link Key} or
     *             {@link Class}.
     * 
     * @see #retainAll(Collection)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default void removeAll(Collection<?> keys) {
        requireNonNull(keys, "keys is null");
        for (Object o : keys) {
            requireNonNull(o, "Specified collection contains a null");
            if (o instanceof Key) {
                keys().remove(o);
            } else if (o instanceof Class c) {
                keys().remove(Key.of(c));
            } else {
                throw new IllegalArgumentException(
                        "The specified collection must only contain instances of " + Key.class.getCanonicalName() + " or " + Class.class.getCanonicalName());
            }
        }
    }

//    /**
//     * @param filter
//     *            a predicate which returns {@code true} for services to be removed
//     * @see Collection#removeIf(Predicate)
//     */
//    default void removeIf(Predicate<? super Service> filter) {
//        requireNonNull(filter, "filter is null");
//        for (Iterator<Service> iterator = iterator(); iterator.hasNext();) {
//            Service s = iterator.next();
//            if (filter.test(s)) {
//                iterator.remove();
//            }
//        }
//    }

    /**
     * Similar to {@link #map(Factory)} except that it will automatically remove all dependencies of the factory once the
     * mapping has finished.
     * 
     * @param factory
     *            the factory
     */
    public abstract void replace(Factory<?> factory);

    default void retain(Class<?>... keys) {
        retain(Key.of(keys));
    }

    default void retain(Key<?>... keys) {
        keys().retainAll(Set.of(keys));
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


    /**
     * @param keys
     * @see #removeAll(Collection)
     * @throws IllegalAccessError
     *             if the collection contains elements that are not either a {@link Class} or a {@link Key}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default void retainAll(Collection<?> keys) {
        requireNonNull(keys, "keys is null");
        Object[] a = keys.toArray();
        for (int i = 0; i < a.length; i++) {
            Object o = a[i];
            requireNonNull(o, "Specified collection contains a null");
            if (o instanceof Class c) {
                a[i] = Key.of(c);
            } else if (!(o instanceof Key)) {
                throw new IllegalArgumentException(
                        "The specified collection must only contain instances of " + Key.class.getCanonicalName() + " or " + Class.class.getCanonicalName());
            }
        }
        keys().retainAll(Set.of(a));
    }
}
