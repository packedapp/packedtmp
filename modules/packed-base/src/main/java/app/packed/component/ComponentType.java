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
package app.packed.component;

/**
 *
 */
public enum ComponentType {
    CONTAINER,

    // Vi har extensionen som en komponent. Primaert fordi det
    // er den letteste maade at analysere paa. Da vi blot koere igennem traet
    EXTENSION,

    // hver funktion er sin egen component...
    // Saa kan man ogsaa lettere have FUCNTION_TYPE
    // Det fungere jo ikke hvis man kan have 20 functioner
    FUNCTION,

    SINGLETON, // 1 instance co-terminus with Container

    PROTOTYPE, // Any number of instances, will Be instantiated by the runtime and delivered whereever it is needed

    STATIC, // no instance

    REQUEST; // instances created and processed by extensions... (Actual instance is never visible though)
}
// FOLDER

// En wirelet kan