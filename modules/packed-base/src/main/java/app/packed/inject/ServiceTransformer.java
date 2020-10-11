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

import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;

/**
 *
 * A service transformer is
 *
 * 
 */
// Altsaa størstedelen af wirelets kan jo bare wrappe saadan en....

// Vil sige at hvert skridt i wirelets transfomration.
// Skal resultere i unikke keys

// Det er jo mere eller mindre...
// de her compute ting
// Tror altsaa bedre jeg kan lide den end wirelets...

// ServiceComputer... nah
public interface ServiceTransformer extends ServiceRegistry {

    ServiceTransformer remove(Key<?>... keys);

    ServiceTransformer retain(Key<?>... keys);

    ServiceTransformer retain(Predicate<? super Service> predicate);

    ServiceTransformer retain(Iterable<? super Key<?>> keys);

    // Hvis eager ikke er godt nok. Saa smid det i en future task...
    // constantify
    // The name of the container...
    //
    // String containerName();

    // Kan vel bare vaere et map som tager et factory der har sig selv som dependecy.
    // If the specified factory has itself as a variable.
    <T> ServiceTransformer decorate(Class<T> key, Function<T, T> comp);

    ServiceTransformer map(Class<?> from, Class<?> to); // Make returned Service Configurable???

    Service mapService(Class<?> from, Class<?> to); // Make returned Service Configurable???

    ServiceTransformer map(Factory<?> factory, int... resolveInternally);

    // Otherwise they are completed resolved instream...
    ServiceTransformer mapResolveInternally(Factory<?> factory, int... variablesToResolveInternally);
}
