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
package app.packed.assembly;

/**
 *
 */

// Ideen er lidt at vi har alle delegates her der bliver merged
public interface AssemblyDescriptor {

    // Will call all DelegatingAssembly.delegateTo
    static AssemblyDescriptor of(Assembly assembly) {
        throw new UnsupportedOperationException();
    }
}
//???
//static AssemblyDescriptor of(Class<? extends Assembly> assembly) {
//    throw new UnsupportedOperationException();
//}

