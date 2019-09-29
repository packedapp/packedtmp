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

    public void install(Host host, ComponentConfiguration cc) {
        throw new UnsupportedOperationException();
    }
}

abstract class UnlinkStrategy {
    public final static UnlinkStrategy FAIL_IF_DEPENDENCIES = null;

    // Ideen er vi har
    // undeploy();{undeploy(FAIL_IF_DEPENDENCIES)

    // undeploy(UnlinkStrategy s);

}
