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
package internal.app.packed.service.build;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.bean.BeanExtensionPoint;
import app.packed.bean.BeanMirror;
import app.packed.container.AbstractComposer;
import app.packed.operation.Op;
import app.packed.service.Qualifier;
import app.packed.service.ServiceLocator;
import internal.app.packed.service.sandbox.Service;

/**
 * Service transformers are typically use to to convert one set of services to another set of services.
 * 
 * <p>
 * Unlike, when for example create a service locator via ServiceExtension. Where the various methods do not need to be
 * order. A service transformation requires that any dependencies are available whenever performing a transformation of
 * some kind.
 * 
 */
// Create services

// Create (Replaces existing services)
// -- Prototype (Factory, Class)
// -- Singleton (Factory, Class, Instance)
// -- Other ServiceLocator) 

// Update
// -- Rekey
// -- Reattribute
// -- Decorate
// -- Replace

// Delete
// -- Remove
// -- Retain

// Read
// -- Via ServiceRegistry

// was ServiceTransformation. But Validation, Conversation...
// ServiceMaker, ServiceSpawner. Only because I want to reserve *Transformer
// ServiceComposer
// For future use

// Bliver noedt til at checke at vi ikke f.eks. bruger @OnStart....
// Altsaa skal vi have en special ServiceConfigurationClass....
// Den configuration vi skal kalde er jo ikke helt ContainerConfiguration
// F.eks. hvis vi bruger den i forbindelse med filterExports eller wirelets
// Det er jo mere en slags tilretning, hvor vi gerne vil registrere nogle componenter...
public abstract /* sealed */ class ServiceComposer extends AbstractComposer /* permits PackedServiceComposer */ {

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
    public <T> void decorate(Class<T> key, Function<? super T, ? extends T> decoratingFunction) {
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
    public abstract <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction);

    // prototype or constant...
    // Hvis vi skal bruge den som en Builder...
    // will override any existing service
    // will be a constant if every single dependency is a constant
    // will be resolved against the realm in which the wirelet is being used
    // if used on root. Must use Factory#withMethodHandle unless public exposed to everyone\
    // will decorate a service injected as itself

    public void map(Class<?> implementation) {
        map(BeanExtensionPoint.factoryOf(implementation));
    }

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

    public abstract void map(Op<?> factory);

    public void prototype(Class<?> implementation) {
        prototype(BeanExtensionPoint.factoryOf(implementation));
    }

    public abstract void prototype(Op<?> factory);

    public void provide(Class<?> implementation) {
        provide(BeanExtensionPoint.factoryOf(implementation));
    }

    public abstract void provide(Op<?> factory);

    // addAll??? nogle af dem er jo prototypes.
    /**
     * @param locator
     *            the locator to provide services from
     */
    public void provideAll(ServiceLocator locator) {
        throw new UnsupportedOperationException();
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
    public <T> void provideInstance(Class<T> key, T instance) {
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
    public abstract <T> void provideInstance(Key<T> key, T instance);

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
    public void provideInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        provideInstance((Class) instance.getClass(), instance);
    }

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
    public void rekey(Class<?> existingKey, Class<?> newKey) {
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
    public abstract void rekey(Key<?> existingKey, Key<?> newKey); // Return the new service????

   

    /**
     * <p>
     * Keys for which a corresponding service is not present, are ignored.
     * 
     * @param keys
     *            the keys that should be removed
     */
    public void remove(Class<?>... keys) {
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
    public void remove(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        for (Key<?> k : keys) {
            requireNonNull(k, "key in specified array is null");
            keys().remove(k);
        }
    }

    public abstract Set<Key<?>> keys();
    
    /** Removes all services. */
    public void removeAll() {
        keys().clear();
    }

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
    public void removeAll(Collection<?> keys) {
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

    /**
     * @param filter
     *            a predicate which returns {@code true} for services to be removed
     * @see Collection#removeIf(Predicate)
     */
    public void removeIf(Predicate<? super Key<?>> filter) {
        requireNonNull(filter, "filter is null");
        for (Iterator<Key<?>> iterator = keys().iterator(); iterator.hasNext();) {
            Key<?> s = iterator.next();
            if (filter.test(s)) {
                iterator.remove();
            }
        }
    }

    public void replace(Class<?> implementation) {
        replace(BeanExtensionPoint.factoryOf(implementation));
    }

    /**
     * Similar to {@link #map(Op)} except that it will automatically remove all dependencies of the factory once the
     * mapping has finished.
     * 
     * @param factory
     *            the factory
     */
    public abstract void replace(Op<?> factory);

    public void retain(Class<?>... keys) {
        retain(Key.of(keys));
    }

    public void retain(Key<?>... keys) {
        keys().retainAll(Set.of(keys));
    }

    /**
     * @param keys
     * @see #removeAll(Collection)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void retainAll(Collection<?> keys) {
        requireNonNull(keys, "keys is null");
        Object[] a = keys.toArray();
        for (int i = 0; i < a.length; i++) {
            Object o = a[i];
            requireNonNull(o, "Specified collection contains a null");
            if (o instanceof Class) {
                a[i] = Key.of((Class) o);
            } else if (!(o instanceof Key)) {
                throw new IllegalArgumentException(
                        "The specified collection must only contain instances of " + Key.class.getCanonicalName() + " or " + Class.class.getCanonicalName());
            }
        }
        keys().retainAll(Set.of(a));
    }
}

// Various ideas on provide/rekey
abstract class YIdeas {

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

    // void addAttributeAll(Key, attributes);

    //// Fcking Qualifier not an attribute
    // void addAttributeName(Key, String name);
    // void addAttributeNameAll(Key, String name);
    // Ideas for consta fying things...
    // Maybe we have some special decorators????
    // Or maybe just methods...
    /// decorate(Foo.class, ServiceTransformer.CONSTAFY)
    /// decorate(Foo.class, ServiceTransformer.UNCONSTAFY)
    public abstract void addName(Function<Service, String> nameFunction);

    /**
     * Adds.
     * 
     * If a service already has a name qualifier this method will override the existing value
     * 
     * @param name
     */
    public void addNameAll(String name) {
        // rekeyAll(k -> k.key().withTag(name));
    }

    // Maybe mirror key names
    public void addQualifierAll(Annotation qualifier) {
        // rekeyAll(s -> s.key().with(qualifier));
    }

    // provide(new Foo<>(e->e)); eller
    // protoype(new Foo<>(e->e)); eller
    // Det er jo sjaeldent man bare vil lave den til en konstant.
    // Som regel vil man gerne argumentere den
    public void constify(Key<?> key) {}

    public abstract void qualifyWith(Class<?> k, Annotation qualifier);

    public abstract void qualifyWith(Key<?> k, Annotation qualifier);
    // alias()??

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
    // Vi er ihvertfald interesset i navn, Container osv.
    // ComponentDesc
    public abstract BeanMirror target();
    // the target (container) of the transformation

    // Vi kan ikke rigtig have source... Idet vi jo er ved at opbygge den container...

}

abstract class ZBadIdeas {

    public abstract Object attachment();

    // wirelets can communicate here???
    // Nah make an AtomicReference... og sa lambda capture

    public abstract ZBadIdeas map(Class<?> from, Class<?> to); // Make returned Service Configurable???

    public abstract ZBadIdeas map(Op<?> factory, int... resolveInternally);

    // Eller ogsaa skal vi have endnu en lag
    // Foerend alle services bliver brugt....
    // Syntes ikke den her fin
    public abstract ZBadIdeas mapResolveInternally(Op<?> factory, int... variablesToResolveInternally);

    public abstract Service mapx(Class<?> from, Class<?> to); // Make returned Service Configurable???

    // JPMS-> Record must be readable for Packed
    // Multiple incoming services -> Multiple outgoing services... Don't think I'm a fan
    // Man maa lave noget midlertigt hulumhej, som ma saa remover
    public abstract ZBadIdeas multiMap(Op<? /* extends Record */> factory, int... resolveInternally);

    // Kan vel bare vaere et map som tager et factory der har sig selv som dependecy.
    // If the specified factory has itself as a variable.

    // Kunne kun bruges i forbindelse med wirelets eller exportTransformation
    // Men folk maa kunne arbejde udenom paa en anden maade.
    // f.eks. er det jo ikke noget problem for toWirelets.. der faar man alt.

    // Otherwise they are completed resolved instream...
    // I think I would rather have something like
    // se.pushForChildExportTransformartion(Key... keys);

    public abstract ZBadIdeas retainIf(Iterable<? super Key<?>> keys);

}
