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
package app.packed.container.extension;

/**
 * A composable extension is a specific type of extension that allows extensions to communicate across of containers.
 */
public abstract class ComposableExtension<T extends ExtensionComposer<?>> extends Extension {

    // Alternative kraeve at man installere en ServiceLoader.... for extensionen composeren....

    // List<ExtensionDescriptor> Extension.findAll(MethodHandle.Lookup caller)
    // Only return those extensions that are readable from the caller
    // Maybe drop caller and use StackWalker....

    // Saa kan vi ogsaa kraeve en Extension::supplier

    // Og tage en access(MethodHandler.lookup) object

    // Eneste problem er classloading.... Vi bliver jo noedt til at loade hver en klasse...
    // For at se hvilken extension den modsvarer... Det gaar ikke...
    // Selvom det ville vaere niiiice.
}
