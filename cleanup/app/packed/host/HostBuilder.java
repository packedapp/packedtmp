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

import app.packed.contract.Contract;
import app.packed.inject.ServiceConfiguration;

/**
 *
 */
// HostConfiguration...
public interface HostBuilder {

    // Vi skal havde fundet ud af hvordan vi kan smide et object med...
    // Objekter er vel mandatory???

    default <T> ServiceConfiguration<T> provide(T instance) {
        throw new UnsupportedOperationException();
    }

    // Her er det interessant med meta data hjaelp omkring contracten....
    // Hvad skal med i Host, for example, Her taenker jeg ogsaa paa bruger ting...
    HostBuilder implementsAtLeast(Contract contract);

    HostBuilder implementsExact(Contract contract);

    // Maybe we can allow to add a specific @Provides as a mixin...
}
// Standalone host...

// Definitely some objects...
// Hmm, should we call them something??????
// I guess it is not components....

// Component Host (maybe latere versions)
