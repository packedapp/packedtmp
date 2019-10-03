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

import app.packed.component.ComponentConfiguration;

/**
 *
 */
// Ideen er man kan lave en accessor via ExtensionCompoer
// ExtensionComposer.newHostAccessor(Class<T> type)

// Og saa kan man kommunikere gennem den

// Skal selvfoelgelig hedde noget andet hvis man ogsaa kan kommunikere med sieblings...
// Eller maaske gaar kommunikationen med sieblings via en host....
/// Dvs service udfra artifact, ind og vende paa en host, og saa tilgaengelig til nye deploys..
/// Vi kan maaske endda lave noget proxy. Saa den kan forsvinde igen...........
// Hmmmm
// All Serializere lortet og maal alt trafik...

// Tjah det giver jo bedre mening....
// Saa har vi heller ikke 1 million services af den samme type
// deploy (ddd, map(Service, @Foo("h1") Service)
// deploy (ddd, map(Service, @Foo("h2") Service)
final class HostAccessor<T> {

    public T get(Host host) {
        // Boer vaere en maade saa man ogsaa kan faa fra sieblings

        // Men det skal vaere configurerbart taenker jeg...
        // hvis man har en million artifacts, ville man helst ikke iterere igennem dem.
        // Det er maaske ogsaa kun services det giver mening for...
        // Og saa maaske Lifecycle, hvis man er linked
        // F.eks. AOP virker ikke
        //

        // Man kan saa paa en eller anden maade link de 2 artifacts sammen...
        throw new UnsupportedOperationException();
    }

    public void install(Host host, ComponentConfiguration<?> cc) {
        throw new UnsupportedOperationException();
    }
}

abstract class UnlinkStrategy {
    public final static UnlinkStrategy FAIL_IF_DEPENDENCIES = null;

    // Ideen er vi har
    // undeploy();{undeploy(FAIL_IF_DEPENDENCIES)

    // undeploy(UnlinkStrategy s);

}
//// Et stort problem med hosts/host accessors, er at vi foerst kan finde dem paa instantierings tidspunktet hvis vi er
//// et image...
/// Dvs. vi kan ikke installere en service fra en parent host.

// Istedet for har vi f.eks. i en parent host.
// Foo[RealFoo]

// Vi installere saa en ny Foo i den nye artifact. Og saa finder vi RealFoo via HostAccessor