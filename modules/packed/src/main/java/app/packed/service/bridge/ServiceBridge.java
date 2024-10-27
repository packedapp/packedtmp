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
package app.packed.service.bridge;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.operation.Op;

/**
 *
 */
public interface ServiceBridge {

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
     * If the service that is being decorated is constant. The function will be invoked at most. Put this else, it is the
     * same for peek, ect.
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
     * <p>
     * The {@link #replace(Op)} if similar, except that it will remove all services that the the op takes as input.
     *
     * @param op
     *            the op
     * @see #replace(Op)
     */
    void map(Op<?> op);

    // Ahhh not open
    void map(Class<? extends Record> recordClass);

    // Ideen er lidt at det er en fake guestBean
    // replaceAll(MyRec.class);
    // Men, har bare ingen MH. Saa tror vi er tilbage til guest bean
    void replaceAll(Class<? extends Record> recordClass);

    default <T> void peek(Class<T> key, Consumer<? super T> consumer) {
        peek(Key.of(key), consumer);
    }

    /**
     * @param <T>
     *            the type of key
     * @param key
     *            the key
     * @param consumer
     *            a consumer that will be invoked whenever the service is provided
     */
    // Should we have one that takes a Provide context?
    <T> void peek(Key<T> key, Consumer<? super T> consumer);

    /**
     * Removes services with any of the specified keys.
     * <p>
     * Keys for which a corresponding service is not present, are ignored.
     *
     * @param keys
     *            the keys that should be removed
     * @see #remove(Key...)
     */
    default void remove(Class<?>... keys) {
        remove(Key.ofAll(keys));
    }

    default void remove(Collection<Key<?>> keys) {
        for (Key<?> key : keys) {
            keys().remove(key);
        }
    }

    default void remove(Iterable<Class<?>> keys) {
        for (Class<?> cl : keys) {
            keys().remove(Key.of(cl));
        }
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
    default void removeAll() {
        keys().clear();
    }

    default void removeIf(Predicate<? super Key<?>> filter) {
        keys().removeIf(filter);
    }

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
    default <T> void replace(Class<T> key, T instance) {
        replace(Key.of(key), instance);
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
    <T> void replace(Key<T> key, T instance);

    /**
     * Similar to {@link #map(Op)} except that it will automatically remove all dependencies of the factory once the mapping
     * has finished.
     *
     * @param factory
     *            the factory
     */
    default void replace(Op<?> op) {
        map(op);
        for (Variable v : op.type().parameterArray()) {
            remove(v.asKey());
        }
    }

    default void retain(Class<?>... keys) {
        retain(Key.ofAll(keys));
    }

    default void retain(Key<?>... keys) {
        keys().retainAll(Set.of(keys));
    }

    public interface ToChild {

    }

    public interface ToParent {

    }
}

// Is this usefull. Probably not
//<T> void decorateAll(BiFunction<? super Key<?>, ? super T, ? extends T> decoratingFunction);
