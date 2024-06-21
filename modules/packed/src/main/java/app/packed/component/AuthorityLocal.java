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
package app.packed.component;

import app.packed.build.BuildLocal;

/**
 *
 */
// ContainerScope, ApplicationScope
// Do we support stuff that is not components????
// Det er vel mere paa Bean niveau. Giver ikke mening paa container/application scope

// Jeg tror faktisk ikke den giver mening.
// Maaske vi kan have den paa BuildLocal??? get(Authority, );
// Alternativt, har folk bare et HashMap<Authority, ) paa en BuildLocal

// USECASES????
// I think, for example, ServiceNamespace


// Maybe it is a special ApplicationLocal.of(authority) instead
// I think I like the this instead of a separate interface

public non-sealed interface AuthorityLocal<T> extends BuildLocal<AuthorityLocal.AuthorityLocalAccessor, T> {

    // Explicitly on the authority
    T get(Authority authority); //wtf???

    // All omkring beans vil jeg mene...
    // Extension???
    interface AuthorityLocalAccessor {}
}
