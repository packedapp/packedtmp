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
package app.packed.service.mirrorold;

import java.util.stream.Stream;

import app.packed.bean.BeanMirror;
import app.packed.binding.Key;
import app.packed.component.ComponentRealm;
import app.packed.context.ContextMirror;
import app.packed.operation.OperationMirror;
import app.packed.service.mirror.ServiceBindingMirror;
import app.packed.service.sandbox.ServiceProviderKind;

/**
 *
 */

// Har vi et BeanNamespace??? Og hvis vi har peger vi altid paa services derfra?

// Hvis ikke, har vi saa services tilgaengelig fra forskellige namespaces???
// Baade Bean og Container

// Hvad hvis den samme service er tilgaengelig under flere keys...

// Det man gerne vil have svar er jo fx hvor i applikationen bliver denne bean brugt som en service...

// Can search for providers where bindings.count==0 -> To find unused providers
public interface ServiceProviderMirror {

    // Returns the bindings where this particular service is used under the specified key.
    /** {@return a stream of all bindings that are bound to this service provider} */
    Stream<ServiceBindingMirror> bindings();

    /** {@return the key under which the service is available.} */
    Key<?> key();

    ServiceProviderKind kind();

    // Skal der owner paa alt nu???
    // filter.cliCommands().owned
    //
    ComponentRealm providedBy();

    // I don't think so, call distinct on bindings();
    ComponentRealm providedTo();

    interface FromOperation {

        /** {@return the operation the service is provided to} */
        OperationMirror operation();
    }

    interface FromBean {
        BeanMirror bean();
    }

    interface FromContext {
        ContextMirror context();
        // contextualized object?
    }

    interface FromServiceNamespace {
        BeanMirror providingBean(); // IDK optional

        OperationMirror providingOperation(); // IDK optional

     //   ServiceNamespaceMirror namespace();
    }
    // FromImportExportMappedStrangeThings, idk
}
