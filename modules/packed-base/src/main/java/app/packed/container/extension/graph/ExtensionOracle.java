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
package app.packed.container.extension.graph;

import app.packed.container.extension.Extension;

/**
 *
 */

// Eneste problem er med super artifacts.
// Saa hvis vi er et image. kan vi jo foerst finde ud af naar vi instantiere den
// Men taenker det er mere configuration....

// tjah nah...f.eks. installeringen af en WebServer....
// Saa maa onstart jo bare blive cancelled.....
public interface ExtensionOracle<E extends Extension> {

    int count();

    E root();
}
