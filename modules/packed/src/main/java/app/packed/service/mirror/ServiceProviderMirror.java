/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import app.packed.bean.BeanMirror;
import app.packed.binding.Key;
import app.packed.context.ContextMirror;
import app.packed.operation.OperationMirror;
import app.packed.service.ServiceNamespaceMirror;

/**
 *
 */

// Har vi et BeanNamespace??? Og hvis vi har peger vi altid paa services derfra?

// Hvis ikke, har vi saa services tilgaengelig fra forskellige namespaces???
// Baade Bean og Container

// Hvad hvis den samme service er tilgaengelig under flere keys...

// Det man gerne vil have svar er jo fx hvor i applikationen bliver denne bean brugt som en service...

public interface ServiceProviderMirror {

    /** {@return a stream of all bindings where this service is provided to} */
    Stream<ServiceBindingMirror> bindings();

    /** {@return the key under which the service is available.} */
    Key<?> key();

    // Ideen er lidt at den her viser. Hvilke exports o.s.v. vi skal igennem
    SequencedCollection<Object> servicePath();

    /**
     * @see app.packed.bean.BeanConfiguration#bindCodeGenerator(Key, java.util.function.Supplier)
     * @see app.packed.bean.BeanConfiguration#bindServiceInstance(Key, Object)
     */
    interface FromBean extends ServiceProviderMirror {

        /** {@return the bean the service was bound to} */
        BeanMirror bean();
    }

    interface FromContext extends ServiceProviderMirror {
        ContextMirror context();
    }

    interface FromNamespace extends ServiceProviderMirror {
        // Is there always an operation????

        ServiceNamespaceProvideOperationMirror providedVia();

        ServiceNamespaceMirror namespace();
    }

    interface FromOperation extends ServiceProviderMirror {
        OperationMirror operation();
    }
}

//
///** {@return the namespace the service is available in.} */
//ServiceNamespaceMirror namespace();

///**
//* {@return the provider of the service.}
//* <p>
//* May either be an operation on a bean. The bean itself Or a constant
//*/
//ServiceProviderIsThisUsefulMirror provider();
