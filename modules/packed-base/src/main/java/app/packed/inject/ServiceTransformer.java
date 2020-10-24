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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.base.Key.Qualifier;
import app.packed.base.Nullable;
import app.packed.component.Component;

/**
 *
 * An interface supporting transformation of any number of services.
 * 
 * @apiNote In the future, if the Java language permits, {@link ServiceTransformer} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface ServiceTransformer extends ServiceRegistry {

    default <T> void decorate(Class<T> key, Function<? super T, ? extends T> decoratingFunction) {
        decorate(Key.of(key), decoratingFunction);
    }

    <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction);

    // will override any existing service
    // will be a constant if every single dependency is a constant
    // will be resolved against the realm in which the wirelet is being used
    // if used on root. Must use Factory#withMethodHandle unless public exposed to everyone\
    // will decorate a service injected as itself
    default void provideAll(ServiceLocator locator) {
        throw new UnsupportedOperationException();
    }

    default void provide(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    default void provideInstance(Object instance) {
        throw new UnsupportedOperationException();
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
    // Null???? Why Not.. I don't think we will ever be a inline type
    // Map<Key<?>, Service>... taenker det er en unmodifiable wrapper over en intern
    // datastructur
    // Take text from Map#compute
    default void rekeyAll(Function<Service, @Nullable Key<?>> rekeyingFunction) {
        for (Service s : this) {
            Key<?> key = rekeyingFunction.apply(s);
            if (key == null) {
                remove(key);
            } else if (!key.equals(s.key())) {
                rekey(s.key(), key);
            }
        }
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
    void remove(Key<?>... keys);

    /** Removes all services. */
    default void removeAll() {
        keys().clear();
    }

    /**
     * @param filter
     *            a predicate which returns {@code true} for services to be removed
     * @see Collection#removeIf(Predicate)
     */
    void removeIf(Predicate<? super Service> filter);

    default void retain(Class<?>... keys) {
        retain(Key.of(keys));
    }

    void retain(Key<?>... keys);
}

interface Xincubator extends ServiceTransformer {

    // Ideas for consta fying things...
    // Maybe we have some special decorators????
    // Or maybe just methods...
    /// decorate(Foo.class, ServiceTransformer.CONSTAFY)
    /// decorate(Foo.class, ServiceTransformer.UNCONSTAFY)
}

// Various ideas on provide/rekey
interface YIdeas extends ServiceTransformer {

    void addName(Function<Service, String> nameFunction);

    /**
     * Adds.
     * 
     * If a service already has a name qualifier this method will override the existing value
     * 
     * @param name
     */
    default void addNameAll(String name) {
        rekeyAll(k -> k.key().withName(name));
    }

    // Maybe mirror key names
    default void addQualifierAll(Annotation qualifier) {
        rekeyAll(s -> s.key().withQualifier(qualifier));
    }

    // Kan vel bare vaere et map som tager et factory der har sig selv som dependecy.
    // If the specified factory has itself as a variable.

    ServiceTransformer map(Factory<?> factory, int... resolveInternally);

    // Otherwise they are completed resolved instream...
    // I think I would rather have something like
    // se.pushForChildExportTransformartion(Key... keys);

    // Eller ogsaa skal vi have endnu en lag
    // Foerend alle services bliver brugt....
    // Syntes ikke den her fin
    ServiceTransformer mapResolveInternally(Factory<?> factory, int... variablesToResolveInternally);

    Service mapService(Class<?> from, Class<?> to); // Make returned Service Configurable???

    // Return the same name to avoid any rekeying

    // Will fail if any over

    // Hvis eager ikke er godt nok. Saa smid det i en future task...
    // constantify
    // The name of the container...
    // String containerName();

    //// Throws UnsupportedOperation for root provideTo

    // Source er jo ved at bliver bygget...
    // Component source();
    //// Throws UnsupportedOperation for root provideTo

    // The container we transform to... IDK
    // Maaske har vi brug for noget andet end Component
    // Component2Be
    // Vi er ihvertfald interesset i navn, Bundle osv.
    // ComponentDesc
    Component target();
    // the target (container) of the transformation

    // Vi kan ikke rigtig have source... Idet vi jo er ved at opbygge den container...

}

interface ZBadIdeas extends ServiceTransformer {
    // wirelets can communicate here???
    // Nah make an AtomicReference... og sa lambda capture
    Object attachment();

    /**
     * A version of
     * 
     * @param existingKey
     *            the key of an existing service
     * @param newKey
     *            the new key of the service
     */
    // How useful is this. It is only for downcasting
    // don't really see many usecases
    default void rekey(Class<?> existingKey, Class<?> newKey) {
        rekey(Key.of(existingKey), Key.of(newKey));
    }

    ServiceTransformer map(Class<?> from, Class<?> to); // Make returned Service Configurable???

    ServiceTransformer retainIf(Iterable<? super Key<?>> keys);

    // JPMS-> Record must be readable for Packed
    // Multiple incoming services -> Multiple outgoing services... Don't think I'm a fan
    // Man maa lave noget midlertigt hulumhej, som ma saa remover
    ServiceTransformer multiMap(Factory<? /* extends Record */> factory, int... resolveInternally);

}

//Altsaa størstedelen af wirelets kan jo bare wrappe saadan en....

//Vil sige at hvert skridt i wirelets transfomration.
//Skal resultere i unikke keys

//Det er jo mere eller mindre...
//de her compute ting
//Tror altsaa bedre jeg kan lide den end wirelets...

//ServiceComputer... nah
