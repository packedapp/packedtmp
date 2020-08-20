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
package zandbox;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.base.TypeLiteral;
import app.packed.container.Extension;
import app.packed.container.ExtensionOrdering;
import app.packed.inject.Factory;

/**
 *
 */

// 3 users

// Den extension der laver den
// Folk der bruger den extension
// Extensions der bruger Subtension

//ExtensionList

// Skal vi have en Builder???

//remove, swap, bla, bla
public interface ExtensionDeck<T> {

    Class<? extends Extension> owner();

    // The specified extensionType must be a direct dependency of owner.
    //// The specified Factory must be related to the specified extensionType

    // in some way??? Nah vi kan vel have nogle statiske.

    // Alternativ har vi
    // ExtensionContext
    // Signed<T> sign(T);

    // Signed.signedBy

    // Ide'en er at den bruges fra en Subtension
    void add(Class<? extends Extension> extensionType, T value);

    void addAfter(Class<? extends Extension> extensionType, T value);

    // I think it should fail if the extension has not been registered
    void addBefore(Class<? extends Extension> extensionType, T value);

    // Add before any extension
    // invoking addFirst("A") -> ["A"], addFirst("B") ->["B", "A"]
    void addFirst(T value);

    void addLast(T value);

    /**
     * Returns a stream containing all the entries in this deck.
     * 
     * @return a stream containing all the entries in this deck
     */
    Stream<Entry<T>> entries();

    ExtensionOrdering extensions(); // Any extensions that have been used

    boolean isEmpty();

    default Stream<T> values() {
        return entries().map(e -> e.value());
    }

    interface Entry<T> {

        Optional<Class<? extends Extension>> extension();

        T value();
    }

    interface Builder<T> {

        // Taenker ogsaa brugeren
        Builder<T> onAdd(Consumer<? super Entry<? super T>> action);

        ExtensionDeck<T> build();
    }
}

// Ved ikke rigtig om vi kan lave en generisk lists....
/// Maaske Bare en DecoratorConfiguration + Builder;
/// Skal ogsaa kunne specificere
// Hmmm
interface ExtensionXontext {

    // ret spe

    // Skal kunne specificere en eller anden form for isolator???

    <T> ExtensionDeck<Factory<T>> newDecoratedChain(Class<T> type);

    <T> DecoratorBuilder<T> newDecorator(Class<T> type);

    <T> DecoratorBuilder<T> newDecorator(TypeLiteral<T> type);

    <T> ExtensionDeck.Builder<T> newDeckBuilder(Class<T> type);

    interface DecoratorBuilder<T> {
        ExtensionDeck<Factory<T>> build();
    }
}
