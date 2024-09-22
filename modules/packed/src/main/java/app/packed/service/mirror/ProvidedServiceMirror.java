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
package app.packed.service.mirror;

import java.util.SequencedCollection;
import java.util.stream.Stream;

import app.packed.binding.Key;

/**
 *
 */

// Har vi et BeanNamespace??? Og hvis vi har peger vi altid paa services derfra?

// Hvis ikke, har vi saa services tilgaengelig fra forskellige namespaces???
// Baade Bean og Container


// Hvad hvis den samme service er tilgaengelig under flere keys...

// Det man gerne vil have svar er jo fx hvor i applikationen bliver denne bean brugt som en service...

public interface ProvidedServiceMirror {

    /** {@return the key under which the service is available.} */
    Key<?> key();

    /** {@return the namespace the service is available in.} */
    ServiceNamespaceMirror namespace();

    /**
     * {@return the provider of the service.}
     * <p>
     * May either be an operation on a bean.
     * The bean itself
     * Or a constant
     */
    ServiceProviderMirror provider();

    // Returns the bindings where this particular service is used under the specified key.
    // Hvis en service er tilgaengelig under forskellige keys...
    // Saa er ServiceProviderMirror.bindings nok bedre
    Stream<ServiceBindingMirror> bindings();

    // Ideen er lidt at den her viser. Hvilke exports o.s.v. vi skal igennem
    SequencedCollection<Object> servicePath();
}
