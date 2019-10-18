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
package packed.internal.aaa.extension.graph;

/**
 *
 */
// Maybe a generic DAG<V, E>
// DAG<ExtensionDescriptor, Void>
// DAG<ServiceDescriptor, ?>
// Arghh tror det er bedst med specifikke.
class ExtensionDependencyGraph {

    // Ideen var lidt at kunne visualisere eller lignende relationerne mellem forskellige extensions..

    // Maaske behoever vi ikke denne klasse og kan klare os med ExtensionDescriptor
    // og ExtensionContext

    // Der er jo isaer i forbindelse med at udvikle de her extension, at det kunne vaere rart at
    // se hvordan de enkelte extension relatere.
    // F.eks. soerger for at koere shutdown paa WebExtension, foerend LoggingExtension.
    // Men samtidig tillade at nogle bliver koert parallelt....

    // Tror det giver mening bare at bruge ExtensionDescriptor...
    // Den her graph ville jo ogsaa bare skulle laves fra en single Extension Type...
    // Som man saa tog udgangspunkt i
}
