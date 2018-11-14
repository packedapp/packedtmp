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

import java.util.Map;
import java.util.Set;

/**
 *
 */

// Det gode ved at have en SPEC_VERSION, er at man kan specificere man vil bruge.
// Og dermed kun importere praecis de interfaces den definere...
// Deploy(someSpec?) ved ikke lige med API'en /
// FooBarBundle.API$2_2
// FooBarBundle.API$2_3-SNAPSHOT hmmm, saa forsvinder den jo naar man releaser den???
// Maaske hellere have den markeret med @Preview :D
/// Bundlen, kan maaske endda supportere flere versioner??Som i flere versioner??

public interface ServiceSpecification {

    /**
     * Returns an immutable map of all services that are available for importing.
     * <p>
     * A service whose key have been remapped will have t <pre> {@code
     *  Key<Integer> -> Descriptor<Key<Integer>, "MyService>
     *  importService(Integer.class).as(new Key<@Left Integer>));
     *  Key<Integer> -> Descriptor<Key<@Left Integer>, "MyService>
     *  Note the key of the map has not changed, only the key of the descriptor.}
     * </pre> Eller ogsaa er det kun i imported servicess?????Ja det tror jeg
     *
     * @return a map of all services that available to import
     */
    Map<Key<?>, ServiceDescriptor> exposedServices();

    /**
     * Returns an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity.
     * 
     * @return an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity
     */
    Set<Key<?>> optionalServices();// Description??

    /**
     * Returns an immutable set of all service keys that <b>must</b> be made available to the entity.
     * 
     * @return an immutable set of all service keys that <b>must</b> be made available to the entity
     */
    Set<Key<?>> requiredServices();
}
// Skal vi have noget mere end key???
// F.eks. description????