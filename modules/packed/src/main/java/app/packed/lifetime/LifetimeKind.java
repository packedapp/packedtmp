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
package app.packed.lifetime;

/**
 *
 */
// Er application bare det samme som en root container?
public enum LifetimeKind {

    /** Created and destroyed together with the application. */
    APPLICATION,

    /** Created and destroyed together with a (non-rooted) container. */
    CONTAINER,
    
    /** Created and destroyed independently of other components */
    BEAN;
}

// function/static beans har samme lifetime som deres container



/// Hmm maaske har vi flere LifetimeKind???
// BeanLifetimeKind [Container, OPERATION
// OPERATION, DEPENDANT, ...

// Omvendt saa kan vi vel godt lave en container pga en operation????


//// Honorable mentions


//// Haaber vi kan undgaa at tilfoeje den her
// DEPENDANT,

//// Lazy er ikke paa lifetimen, men paa componenten...
//// Fordi du er jo lazy i forhold til applikationen eller containeren.
// LAZY; 


//// Vi dropper den her fordi vi simpelthen bare siger de ikke har nogen lifecycle
// UNMANAGED (or Epheral)
 