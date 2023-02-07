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
package app.packed.extension.sandbox;

import app.packed.extension.Extension;

/**
 * <p>
 * If you have an instance of ExtensionLookup<E> you can do anything you want for that particular extension
 */
// Hvor skal vi bruges


/// Indtil videre har vi de her 2 use cases
// ContainerLifetimeBridge
// Context
class ExtensionLookup<E extends Extension<E>> {

    static <E extends Extension<E>> ExtensionLookup<E> of(Class<E> extensionClass) {
        return new ExtensionLookup<>();
    }
}
