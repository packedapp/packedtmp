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
package app.packed.service.sandbox;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.service.Injector;
import app.packed.service.InjectorAssembler;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceDescriptor;
import app.packed.service.ServiceTransformer;

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

    public static void main(String[] args) {
        computeWithContract(sc -> removeTo(sc.services().iterator().next()));
    }

    // AV AV. Maaske skal vi kun tillade ServiceWirelets....
    // Problemet er f.eks. ComputeWithContract(c->Wirelet.name("foobar");
    // Det er et ordering problem...
    static Wirelet computeWithContract(Function<? super ServiceContract, @Nullable ? extends Wirelet> function) {
        throw new UnsupportedOperationException();
    }

    // May map to null
    public static Wirelet delayed(Function<Set<ServiceDescriptor>, Wirelet> mapper) {
        throw new UnsupportedOperationException();
    }

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

    public static Wirelet provideAllTo(Consumer<? super InjectorAssembler> configurator, Wirelet... wirelets) {

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

    static Wirelet transformTo(Consumer<? extends ServiceTransformer> transformer) {
        throw new UnsupportedOperationException();
    }
}
