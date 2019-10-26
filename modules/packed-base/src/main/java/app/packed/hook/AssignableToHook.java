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
package app.packed.hook;

import static java.util.Objects.requireNonNull;

import packed.internal.hook.UnreflectGate;

/**
 * Represent an annotated field on a component instance.
 * 
 * 
 * This hook does not distinguish between different generic types.
 * 
 */
// Det skal bruges som plugin arkitektur.
// Hvordan virker det paa kryds af containere???
// Boer vel virke som en receiver. Hvis man levere
// Det man kan bruge den som en parameter til en metode...
// Virker aldrig med componennts der har TypeVariabels

// Hvordan virker det med generiske typer, f.eks. med List<String>
//// Tror ikke vi supportere generiske typer....
// Matchingen er sgu lidt compliceret. Men vi kunne jo godt have en TypeLiteral
// returneret, og saa kan folk selv processere det
// Eller hvad, vi skal vel bare impl

// InstanceOfHook<List<? extends String>

// Does not currently support generic types...

// InstanceOfHook, AssignableToTypeHook
public final class AssignableToHook<T> implements Hook {

    /** The builder for the component type. */
    final UnreflectGate controller;

    /** The actual type. */
    private final Class<? extends T> type;

    /**
     * Creates a new hook instance.
     * 
     * @param controller
     *            the builder for the component type
     * @param type
     *            the actual type that was hook
     */
    AssignableToHook(UnreflectGate controller, Class<? extends T> type) {
        this.controller = requireNonNull(controller);
        this.type = requireNonNull(type);
    }

    /**
     * Returns the instance.
     *
     * @return the instance
     */
    public HookApplicator<T> applicator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the actual type (assignable to T).
     * 
     * @return the actual type
     */
    // Syntes det er rimeligt at klassen er til raadighed,
    // Den er det jo paa runtime
    // Check den virker generics, f.eks. med List
    public Class<? extends T> type() {
        return type;
    }
}
