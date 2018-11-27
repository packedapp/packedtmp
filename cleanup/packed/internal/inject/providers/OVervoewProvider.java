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
package packed.internal.inject.providers;

import packed.internal.util.descriptor.InternalFieldDescriptor;

/**
 *
 */

// MethodHandle, VarHandle, InternalFactory
// Resolved dependencies (unlike InternalFactory) in some form
// Immediately wraps any exception in InternalProvisionExceptions, and saves the node.

// Holds on to descriptor? Node?
public class OVervoewProvider {

    final int type;

    OVervoewProvider(InternalFieldDescriptor field, Object instance) {
        this.type = 0;
    }

    // Internal Factories
    // Fac0
    // Fac1
    // Fac2
    // Bindable

    ////// FIELDS
    // static
    // static + volatile

    // instance
    // instance + volatile;

    // Instance Provider (for example, Provides on a component field and we need the component instance)
    // Instance Provider + volatile

    //// Constuctors (like static)

    ////// METHODS
    // static + Object[]
    // instance + Object[]

    // static + Provider[]
    // instance + Provider[]

    // static + Function<InjectionSite, >[]
    // instance + Function<InjectionSite, >[]

    // static + Function<SyncPoint, >[]
    // instance + Function<Syncpoint, >[]

    // For Provider[] + Function[].
    // Er der to slags fejlmuligheder, en dependency (provider), eller selv method invocationen....

    // Fejlmuligheder
    // Constructeren fejler
    // En dependency fejler f.eks. Factory0 med en runtime exception
    // En dependency fejler med en Exception for invoke smider Throwable.

    // Vi vil gerne registere hvilken exception der er tale om...
    // Maaske pakke alle exceptions in i en InternalProvisionException....

    // An instance of X could not be created because of its dependencies failed to constructed....

    // En function der Tager X istedet for InjectionSite, Syncpoint

}
