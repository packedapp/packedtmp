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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.base.Key.Qualifier;
import app.packed.base.Nullable;
import app.packed.component.Component;

/**
 * Service transformers are typically use to to convert one set of services to another set of services.
 * 
 * <p>
 * Unlike, when for example create a service locator via ServiceExtension. Where the various methods do not need to be
 * order. A service transformation requires that any dependencies are available whenever performing a transformation of
 * some kind.
 * 
 * @apiNote In the future, if the Java language permits, {@link ServiceTransformation} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 * 
 * @see ServiceLocator#transform(java.util.function.Consumer)
 * @see ServiceLocator#of(java.util.function.Consumer)
 * @see ServiceWirelets#to(java.util.function.Consumer)
 * @see ServiceWirelets#from(java.util.function.Consumer)
 * @see ServiceExtension#exportsTransform(java.util.function.Consumer)
 */
// ServiceTransformation? (like action), was ServiceTransformer
public interface ServiceTransformation extends ServiceRegistry {

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

    // prototype or constant...
    // Hvis vi skal bruge den som en Builder...

    // will override any existing service
    // will be a constant if every single dependency is a constant
    // will be resolved against the realm in which the wirelet is being used
    // if used on root. Must use Factory#withMethodHandle unless public exposed to everyone\
    // will decorate a service injected as itself

    // addAll??? nogle af dem er jo prototypes.
    /**
     * @param locator
     *            the locator to provide services from
     */
    default void provideAll(ServiceLocator locator) {
        throw new UnsupportedOperationException();
    }

    // provide a constant via an instance
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
     * Invoking this method is identical to invoking {@code provide(service.getClass(), service)}.
     * 
     * @param instance
     *            the service to provide
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default void provideInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        provideInstance((Class) instance.getClass(), instance);
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
    default void remove(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        for (Key<?> k : keys) {
            requireNonNull(k, "key in specified array is null");
            asMap().remove(k);
        }
    }

    /** Removes all services. */
    default void removeAll() {
        keys().clear();
    }

    /**
     * @param filter
     *            a predicate which returns {@code true} for services to be removed
     * @see Collection#removeIf(Predicate)
     */
    default void removeIf(Predicate<? super Service> filter) {
        requireNonNull(filter, "filter is null");
        for (Iterator<Service> iterator = iterator(); iterator.hasNext();) {
            Service s = iterator.next();
            if (filter.test(s)) {
                iterator.remove();
            }
        }
    }

    default void retain(Class<?>... keys) {
        retain(Key.of(keys));
    }

    default void retain(Key<?>... keys) {
        keys().retainAll(List.of(keys));
    }
}

interface TProviding extends ServiceTransformation {

    // som provide med constant er styret af det der kommer ind...
    // in most situations you probably want to use this one

    // auto figure out if constant or prototype

    void map(Class<?> factory);

    /**
     * <p>
     * If the specified factory does not have declare any variables. The new services will have default (constant) scope.
     * 
     * @param factory
     * @throws IllegalStateException
     *             if the specified factory has dependencies that cannot be resolved among available services.
     */
    void map(Factory<?> factory);

    /* ------- */

    void prototype(Class<?> factory);

    void prototype(Factory<?> factory);

    void provide(Class<?> factory);

    void provide(Factory<?> factory);

    void replace(Class<?> factory);

    /**
     * Similar to {@link #map(Factory)} except that it will automatically remove all dependencies of the factory once the
     * mapping has finished.
     * 
     * @param factory
     *            the factory
     */
    void replace(Factory<?> factory);
}

interface UNext extends ServiceTransformation {

    // Den sidste der mangler er jo en maade at aendre attributer paa

    // En hurtig ide var at returnere en mutable service et sted.
    // Og saa tilfoeje attributer direkte

    // Det ville ogsaa aabne op for

    // serviceOf(Key).decorate();
    // serviceOf(Key).as(); (rekey)

    // Men ahh. retur typerne er jeg ikke saa vilde med..
    // f.eks. decorate skal jo returnere den nye..
    // men hvad saa med attributer...

    // Vi har jo en en realm!!!!
    // void addAttribute(Key, attributes);
}

// Various ideas on provide/rekey
interface YIdeas extends ServiceTransformation {

    // alias()??

    // Ideas for consta fying things...
    // Maybe we have some special decorators????
    // Or maybe just methods...
    /// decorate(Foo.class, ServiceTransformer.CONSTAFY)
    /// decorate(Foo.class, ServiceTransformer.UNCONSTAFY)
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

    // provide(new Foo<>(e->e)); eller
    // protoype(new Foo<>(e->e)); eller
    // Det er jo sjaeldent man bare vil lave den til en konstant.
    // Som regel vil man gerne argumentere den
    default void constify(Key<?> key) {}

    Service map(Class<?> from, Class<?> to); // Make returned Service Configurable???

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

interface ZBadIdeas extends ServiceTransformation {
    // wirelets can communicate here???
    // Nah make an AtomicReference... og sa lambda capture
    Object attachment();

    ServiceTransformation map(Class<?> from, Class<?> to); // Make returned Service Configurable???

    ServiceTransformation map(Factory<?> factory, int... resolveInternally);

    // Eller ogsaa skal vi have endnu en lag
    // Foerend alle services bliver brugt....
    // Syntes ikke den her fin
    ServiceTransformation mapResolveInternally(Factory<?> factory, int... variablesToResolveInternally);

    // JPMS-> Record must be readable for Packed
    // Multiple incoming services -> Multiple outgoing services... Don't think I'm a fan
    // Man maa lave noget midlertigt hulumhej, som ma saa remover
    ServiceTransformation multiMap(Factory<? /* extends Record */> factory, int... resolveInternally);

    // Kan vel bare vaere et map som tager et factory der har sig selv som dependecy.
    // If the specified factory has itself as a variable.

    // Kunne kun bruges i forbindelse med wirelets eller exportTransformation
    // Men folk maa kunne arbejde udenom paa en anden maade.
    // f.eks. er det jo ikke noget problem for toWirelets.. der faar man alt.

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

    // Otherwise they are completed resolved instream...
    // I think I would rather have something like
    // se.pushForChildExportTransformartion(Key... keys);

    ServiceTransformation retainIf(Iterable<? super Key<?>> keys);

}

//Altsaa størstedelen af wirelets kan jo bare wrappe saadan en....

//Vil sige at hvert skridt i wirelets transfomration.
//Skal resultere i unikke keys

//Det er jo mere eller mindre...
//de her compute ting
//Tror altsaa bedre jeg kan lide den end wirelets...

//ServiceComputer... nah
