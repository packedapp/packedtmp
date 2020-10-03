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
package app.packed.service;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.inject.Provider;
import app.packed.inject.Service;
import app.packed.inject.ServiceRegistry;

/**
 * A service locator where every service provided is of a similar type.
 */
public interface TypedServiceLocator<S> extends ServiceRegistry {

    // ServiceProvider????
    void forEach(BiConsumer<Service, Provider<S>> action);

    void forEachInstance(Consumer<? super S> action);

    boolean isEmpty();

    // ???? Er ikke sikker paa vi skal have providere her. Kun "descriptore"
    Set<Provider<S>> providers();

    Set<Service> services();

    /**
     * Returns the number of services in this locator.
     * 
     * @return the number of services in this locator
     */
    int size();

    /**
     * Returns an immutable list an instance for each service.
     * 
     * @return an immutable list an instance for each service
     */
    List<S> toInstanceList();

    List<Provider<S>> toProviderList();

    List<Service> toServiceList();
}

//Extends Iterable???
//Skal have providers
//Skal have descriptors
//Skal have instancer
//For en plugin struktur. Skal vi havde adgang til hvilke modul der definere den
//Maaske kan vi smide det paa som en attribute???

//Kan ikke extende ServiceRegistry da den extender ServiceMap som har T findService()... <--- can return max 1

//Service sets in service sets??? IDK

//It is not a set... Because we might have multiple instances of the same type...
//Det er hellere ikke rigtig en collection fordi vi laver maaske nye instanser hver gang...
