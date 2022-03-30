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
package app.packed.lifecycle;

import java.util.function.Supplier;

import app.packed.container.Wirelet;

/**
 *
 */
// Altsaa hvad hvis vi gerne vil give info med til S

// Det jeg taenker er at om extensions vil have en container i Containeren????
// Maaske har man en LifetimeContainer som holder applikationen???
// Eller evt en Bean...
// Det jeg mener er at kan vi bare deploye fx. UserSessionAssembly.
// Eller skal vi wrappe den i en container/bean som WebExtension'en bestemmer?


/// Maaske er det ogsaa bare nogle method handles...
/// Altsaa forventer ikke der er mange der bruger dem....

public interface ContainerLifetimeMap<K, S> {
    
    S make(K key);
    S make(K key, Wirelet... wirelets);
    
    S makeIfAbsent(K key);
    S makeIfAbsent(K key, Wirelet... wirelets);
    
    //// Will keep making a new key...
    S makeSupplied(Supplier<? super K> keySupplier);
    S makeSupplied(Supplier<? super K> keySupplier, Wirelet... wirelets);
}

//GuestInstanceMap
//Jeg tror maaske vi ender paa MapView + nogle factories, IDK
// Vi vil ogsaa gerne have shutdown