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

// Ideen var egentlig at vi gerne ville beholder alle metoder.
// Men f.eks. tilfoeje en ThreadSafe/ThreadUnsafe contract marker

// Bundle skulle meget gerne bare extende denne, med en specification..
// F.eks. saadan noget som kontrakt kan du faa med via en specification..
// Men er det kun fordi vi ikke gider have 2 constructorer i Bundle???
/// Saa syntes jeg maaske ikke den er det vaerd

// Efter vi har faaet extensions er det jo reelt set kun fordi vi vil returnere noget andet end Bundle...
// Maaske bare ikke lave den final..... Hej... den er jo ikke direkte paa bundle
public abstract class AbstractBundle extends AnyBundle {

    public AbstractBundle(ContainerConfiguration specification) {
        super(specification);
    }
}
