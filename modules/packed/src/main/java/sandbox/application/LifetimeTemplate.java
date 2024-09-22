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
package sandbox.application;

/**
 *
 */

// Altsaa paa en eller anden maade skal den connectes til en Extension..
// Hvis WebExtension nu ejer SessionLifetimeTemplate så er det jo den der skal instantiere den...

/// Session
/// WebRequest

// Taenker ikke noedvendigvis WebRequest er et child af Session. Hvad hvis vi ikke bruger conceptet
// Omvendt vil vi gerne have det injected. Maaske har vi baade parent og dependant.
// Parent vs Dependant

// Vi vil gerne have fx Application<-SomeContainerLifetime<-AllBeans in the container
// Maaske er det noget runtime ting???

public interface LifetimeTemplate {

    LifetimeTemplate APPLICATION = null;

    // Lifetime er komplet managed af someone else
    LifetimeTemplate FOREIGN = null;

    interface Descriptor {
        String name();
    }

    interface Configurator {

    }
}
