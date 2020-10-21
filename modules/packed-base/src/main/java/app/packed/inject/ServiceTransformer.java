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
 * An interface supporting transformation of any number of services.
 * 
 * @apiNote In the future, if the Java language permits, {@link ServiceTransformer} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface ServiceTransformer extends ServiceRegistry {

}

interface ZFrom {
    // Kan vel bare vaere et map som tager et factory der har sig selv som dependecy.
    // If the specified factory has itself as a variable.
    <T> ServiceTransformer decorate(Class<T> key, Function<T, T> comp);

    ServiceTransformer map(Class<?> from, Class<?> to); // Make returned Service Configurable???

    ServiceTransformer map(Factory<?> factory, int... resolveInternally);

    // Otherwise they are completed resolved instream...
    ServiceTransformer mapResolveInternally(Factory<?> factory, int... variablesToResolveInternally);

    Service mapService(Class<?> from, Class<?> to); // Make returned Service Configurable???

    // Hvis eager ikke er godt nok. Saa smid det i en future task...
    // constantify
    // The name of the container...
    //
    // String containerName();

    /**
     * <p>
     * Keys for which a corresponding service is not present, are ignored.
     * 
     * @param keys
     *            the keys that should be removed
     * @return this transformer
     */
    ServiceTransformer remove(Class<?>... keys);

    ServiceTransformer remove(Key<?>... keys);

    ServiceTransformer remove(Predicate<? super Service> predicate);

    ServiceTransformer retain(Iterable<? super Key<?>> keys);

    ServiceTransformer retain(Key<?>... keys);
}
//Altsaa størstedelen af wirelets kan jo bare wrappe saadan en....

//Vil sige at hvert skridt i wirelets transfomration.
//Skal resultere i unikke keys

//Det er jo mere eller mindre...
//de her compute ting
//Tror altsaa bedre jeg kan lide den end wirelets...

//ServiceComputer... nah
