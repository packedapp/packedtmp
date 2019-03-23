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
package app.packed.bundle.x;

import app.packed.bundle.Contract;

/**
 *
 */
// Maaske have en allow dynamic child components naar man registerer componenten...

public abstract class AbstractHost {

    // Bliver noedt til at passe til de services der er available hos componenten....
    abstract Contract contract();
    // Styr hvordan den skal deployes??
    // F.eks. i ny classLoader

    abstract String newName();// Naming strategy for apps.
}
