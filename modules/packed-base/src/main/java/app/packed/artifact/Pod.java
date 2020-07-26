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
package app.packed.artifact;

/**
 * Atomic unit. Normally an artifact. But can be multiple artifacts with a single root. Or it can be just a component
 * (for example an actor).
 * <p>
 * If any part of a pod fails to assemble or initialize the whole pod fails
 */
//Kan vaere et Bundle. eller bare en component
// Men taenker at vi kun supportere noget med en enkelt rod....

// Er en pod en assembling time only??? Build-time only???
// Fejler hele poden hvis initialisering fejler?? Jeg ville sige ja

// Taenker det er et build-time project...

// Kan man afinstallere noget af den????

// Delay Assembling
// Delay Initialization
// Delay Start

// Altsaa en UseCase er jo Session. Syntes det giver god mening at man synchronous laver den
// inde man gaar videre. Hvis man har et image er det jo ikke noget problem...

interface Pod {
    Class<?> root();
}

/// HostConf
///// Link -> Permanent...
///// Add -> SemiPermanent... (but still the same pod)