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

import java.util.function.Function;

import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import app.packed.contract.Contract;
import app.packed.inject.ProvidedComponentConfiguration;

/**
 *
 */
// HostConfiguration...
public interface HostBuilder {

    // Taenker det er her et BundleImage kan shine..
    // BundleImage = Bundle + WiringOptions

    // Function<String, Bundle + WiringOptions> lazyDeployer
    // or BiConsumer(String, Host);

    // Lazyly deploy apps....
    public void supplyBy(Function<String, Bundle> function, Wirelet... operations);
    // Maybe have operations seperate

    public void addOptionsFirst(Wirelet... operations);

    public void addOptionsLast(Wirelet... operations);

    // Vi skal havde fundet ud af hvordan vi kan smide et object med...
    // Objekter er vel mandatory???

    default <T> ProvidedComponentConfiguration<T> provide(T instance) {
        throw new UnsupportedOperationException();
    }

    // Her er det interessant med meta data hjaelp omkring contracten....
    // Hvad skal med i Host, for example, Her taenker jeg ogsaa paa bruger ting...
    HostBuilder implementsAtLeast(Contract contract);

    HostBuilder implementsExact(Contract contract);

    // Maybe we can allow to add a specific @Provides as a mixin...

    // isFullyEncapsulated
    // Paths, Provision<> ect. is a black vox when it reaches a host.
    // We are not trying to provide complete container isolation....
    // Use docker for that...
    public boolean isStrict();

    /// FIXED Bundle, Modifiable, Removeable, ect.
    // Analyzer bootstrap bundles...
    /// Vi skal kunne analyzere en kaempe application paa test-tid. Og saa kun aabne en port.
    // 50.000 services, startup tid, bare aabne en port, og saa koerer vi.
    // Taenker nogen af dem skal kunne leveres som module filer...

    // parallel deployment af apps....
}
// Standalone host...

// Definitely some objects...
// Hmm, should we call them something??????
// I guess it is not components....

// Component Host (maybe latere versions)
