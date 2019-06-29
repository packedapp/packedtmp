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
package app.packed.app;

import app.packed.container.ContainerBundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;

/**
 *
 */
// Host... Something that hosts app

// We should also be able to have a standalone host??
// Host
//// App1
//// App2
//// App3
// ???? But I think that would mean that Host needs lifecycle controls... Which I'm a bit reluctant to add.

// So Maybe
// Container
//// Host
interface Host {

    public static void main(ContainerBundle b) {}

    public static Host of(ContainerSource source) {
        throw new UnsupportedOperationException();
    }

    public static void addHost(ContainerConfiguration cc, ContainerBundle bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // public static void addHost(ContainerConfiguration cc, ContainerSource source, Key<? super Host> key, Wirelet...
    // wirelets) {
    // throw new UnsupportedOperationException();
    // }

    // AnyBundle fungere ikke.. Den har alt for meget lort....

    // Alternativ til AnyBundle????

    // __Det er en "almindelig" komponent__

    //// Har adgang til alle services... Paanaer Host....
    //// Alle de der hooks... De kan jo lige pludselig komme og gaa....
    //// Eller ogsaa skal vi eksplicit sige hvilke vi tillader....

    public static HostConfiguration installHost(ContainerConfiguration cc) {
        throw new UnsupportedOperationException();
    }

    // Exports er altid hvad der kommer ud af containeren
}
