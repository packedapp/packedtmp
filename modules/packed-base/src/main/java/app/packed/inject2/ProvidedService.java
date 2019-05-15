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
package app.packed.inject2;

import app.packed.config.ConfigSite;

/**
 *
 */
// Just like you cannot fool the module system...
// You cannot fool Packed.
//
// Lots of final classes...
//
// ProvidedService information about how the service was created/provided.
// Is final, you cannot cheat...
// Maybe have some fake/test mode....
//
//
// Provideren får at hvad der _kan_ intercepte.
// ProvidedService får at vide, hvem der interceptet....
// Ideen er her at du kan faa detaljer om hvordan du har faaet services...

// Debug info

// Maaske kan man faa hele path'en. Med mindre hosten er konfigureret som strict
final class ProvidedService<T> {

    ProvidedService() {}
    // Ideen er vi kan trace hvor en service kommer fra....
    // Den er final, package protected, så man ikke kan fake det....

    public ConfigSite configSite() {
        // Ja det er vel hvor den er defineret den source man faar...
        throw new UnsupportedOperationException();
    }

    public T get() {
        throw new UnsupportedOperationException();
    }

    public boolean isPrototype() {
        // true == get() might (@provides could provide the same instance every time) return a new instance every time, false =
        // get return the same instance every time....
        return false;
    }

    /**
     * Returns the module that provides the service.
     * 
     * @return stuff
     */
    public Module sourceModule() {
        // Hmm, vi kan jo ikke f.eks. give Source app'en med.
        // Eftersom vi ikke maa give en reference til en evt. host
        // Det samme med en source component.
        // Saa Vi kan kun give et module med...
        throw new UnsupportedOperationException();
    }
    // interceptore... er nederen hvis man kan cache per app/container/instance/...

    // Vi boer have det med a.la. noget caching... saa vi ikke kun cacher for @Produces....

    // I sidste ende bestemmer bundle der aggregere dem....

    /// Vi kan checke om man maa tilfoeje bundlen..
    // Hvorfor ikke bare soerge for at man ikke kan instantiere bundlen....Hvis man ikke har rettigheder...
    // Bundle
    // protected void addedTo(BundleDescriptor other)....
}
