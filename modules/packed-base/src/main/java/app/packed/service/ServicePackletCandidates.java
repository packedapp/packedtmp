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
package app.packed.service;

import java.util.function.Predicate;

import app.packed.artifact.ArtifactComposer;
import app.packed.base.Key;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;

/**
 *
 */
// 4 basic types of operation

// Provide
// Map will take services and remove existing and output a new one
// Filter
// Peek

// Map, Provide -> (Singleton if all singleton, otherwise... Men ville masske vaere rart at kunne styre

// Mangler support for Contracts
// Mangler support for at kunne specificere providePrototype ect....
public class ServicePackletCandidates {

    // provideTo -> Keeps original
    // provideFrom -> Keeps original

    // provideInstanceTo

    // filterTo (Keys.., Classes
    // filterFrom

    // retainTo()
    // retainFrom();

    public static Wirelet removeTo(Class<?>... keys) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet removeTo(Key<?>... keys) {
        throw new UnsupportedOperationException();
    }

    // Maaske remove istedet for???
    // Altsaa stream() siger hvad man skal include....
    public static Wirelet filterTo(Predicate<? extends ServiceDescriptor> filter) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet provideAllTo(ArtifactComposer<? super InjectorAssembler> configurator, Wirelet... wirelets) {

        // Den her kan man styre prototyper o.s.v. hvis man har behov for det....

        // Ideen er at man kan lave noget med requirements or provides.....
        // En injector
        // Godt nok advancerede....
        throw new UnsupportedOperationException();
    }

    public static Wirelet provideAllTo(Injector injector, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet provideInstanceTo(Object instance) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet provideTo(Factory<?> mapper) {
        throw new UnsupportedOperationException();
    }
}
