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
package app.packed.operation;

import java.lang.invoke.MethodHandle;

/**
 *
 */

// Den bliver provided direkte som keyed...
// Saa den er tilgaengelig 

// Hvorfor er det vi ikke bare kan injecte MethodHandle[]? som key

public interface OperationPack {

    /** {@return an array of all method handles that where prepared.} */
    MethodHandle[] methodsHandles();
}
