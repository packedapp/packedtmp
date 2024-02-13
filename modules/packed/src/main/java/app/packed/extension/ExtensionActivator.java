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
package app.packed.extension;

/**
 *
 */
// Problemet er her mother fucking if assembly is open
@interface ExtensionActivator {
    String extensionName();

    String innerClassName();
}

// Must be an inner class...
// In this way we have the original extension

// Maybe just a Consumer.. Then we use it both for assemblies and extensions
interface ExtensionActivated<E extends ExtensionPoint<?>> {
    void onActivation(E extension);
}
