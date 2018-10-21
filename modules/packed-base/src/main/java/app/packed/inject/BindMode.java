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
package app.packed.inject;

/** A caching mode can be used for */
// At some point we might have a caching framework that will also have a CachingMode
// So might call it something else
// can always cache things in component.store()
// ServiceCachingMode??? Or just ServiceCaching <--

// Per Component, giver jo ogsaa kun mening, hvis jeg producere noget lokalt til en komponent.
// Eller f.eks. en logger

// Skal vi supportere noget der ikke bliver nedarvet til andre injectore.
// Maaske skal de overhoved ikke vaere hirachiske injectore.
// Kun containerene, men ikke de enkelte komponenters

// Det her er foranledigt af at vi gode vil have et ActorSystem til ting der er annoteret med
// @Actor... Qualifier???? Nej, fordi qualifier-> Skal mappe til en.
// @Actor er mere end slags marker. Det kunne ogsaa @Job, eller en speciel metode...
// Basalt set
public enum BindMode {

    /**
     * A single instance is created together with the injector where it created. This is the default used throughout this
     * library.
     */
    EAGER,

    /** A single instance is created on demand. Calls by other threads while constructing the value will block. */
    LAZY,

    /** Instances are created every time a value is requested. */
    NEVER;

    public boolean isSingleton() {
        return this != NEVER;
    }
}
