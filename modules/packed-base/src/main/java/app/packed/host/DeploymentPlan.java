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
package app.packed.host;

/**
 *
 */

// Hvordan man deployer ting paa en host. Hvis det er virkelig kompliceret....

// Service Proxies

// Kan det vaere en ContainerSource????????
/// Saa man kan sige App.run();
// Nah tror heller man kan side det paa en host.
// Gerne paa bygge tidspunktet, saa vi kan validere....
abstract class DeploymentPlan {

    // Forest den her service, saa den her service.
    // Saa afvent at den er startet op,
    // saa skriv "Alle services startede"

    // Lyt paa det her direktorie for reload..
    // Goer dit, goer dat

    abstract void configure();
}
