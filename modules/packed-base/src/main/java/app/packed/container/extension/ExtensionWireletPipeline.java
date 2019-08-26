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
 *
 */
public abstract class ExtensionWireletPipeline<T extends Extension> {

    // Skal vi tage en build context??
    protected abstract ExtensionWireletPipeline<T> split();

    public void buildArtifact() {
        // extension.buildBundle(null);
    }

    public void buildBundle() {}

}

// Man kalder nu ikke laengere en metode paa extension'en
// Men paa det her objekt!>!@>!@{P???
//// Hmmmmmm

// IStedet for at lave noget kompliceret hullumhej
// Er det wirelet context'ens ansvar naar den foerst er blevet aktiveret...

/// Vi kan jo nu faktisk i fremtiden tillade os at tage wirelets til at lave bundle....
/// eftersom vi har build bundle her....

// protected final T extension() {
// return extension;
// }

// onMethods()... istedet for direkte paa