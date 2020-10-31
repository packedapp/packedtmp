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
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.base.Attribute;
import app.packed.base.Key;

/**
 * A specialization of {@link ServiceLocator} where all service instances have a common super type {@code <S>}.
 * Instances of this interface are normally created via the various select methods on ServiceLocator.
 * 
 * @see ServiceLocator#selectAll()
 */

// Metoder

// forEach
// Stream?
// ToList/ToMap

// Instance
// Provider
// Service + Instance
// Service + Provider

// Hmm hvordan haandtere vi Injector????
// Vi bliver noedt til ogsaa at have den paa plads..
public interface ServiceSelection<S> extends ServiceLocator {

    void forEachInstance(BiConsumer<? super Service, ? super S> action);

    void forEachInstance(Consumer<? super S> action);

    /**
     * Returns a stream of all instances in the selection.
     * 
     * @return a stream of all instances in the selection
     */
    Stream<S> instances();
}

interface Zandbox<S> extends ServiceSelection<S> {

    void forEachProvider(BiConsumer<? super Service, ? super Provider<S>> action);

    // does not support regexp
    ServiceSelection<S> named(String name);

    // select(Foo.class).withName()
    // select().withName()

    Stream<Provider<S>> providers();

    ServiceSelection<S> qualifiedWith(Annotation qualifier);

    ServiceSelection<S> qualifiedWith(Class<? extends Annotation> qualifier);

    Stream<Map.Entry<Service, S>> serviceInstances();

    Stream<Map.Entry<Service, S>> serviceProvides();

    /**
     * Returns an immutable list containing a provided service instance for every service in this selection in any order.
     * 
     * @return an immutable list containing a provided service instance for every service in this selection in any order
     */
    // Tror vi skal fikse ServiceRegistry.toList();
    List<S> toInstanceList();

    Map<Key<? extends S>, S> toMapKeyInstances();

    // Only those services which has the s
    // has a qualifier

    Map<Key<? extends S>, Provider<S>> toMapKeyProviders();

    List<Provider<S>> toProviderList();

    <T> ServiceSelection<S> withAttribute(Attribute<T> attribute, Predicate<? super T> filter);

    <T extends Annotation> ServiceSelection<S> withQualifier(Class<? extends T> qualifier, Predicate<? super T> filter);
}
//It is not a set... Because we might have multiple instances of the same type...
//Det er hellere ikke rigtig en collection fordi vi laver maaske nye instanser hver gang...

// <T extends Collection<? super S>> T addTo(T collection);

// void putIntoTo(Collection<? super S> collection);
// Service is a bad key
//Map<Service, S> toMapServiceInstances();
//
//Map<Service, Provider<S>> toMapServiceProviders();
