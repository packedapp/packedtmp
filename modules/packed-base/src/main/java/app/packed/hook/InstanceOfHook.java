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

import java.util.List;

import packed.internal.container.model.ComponentModel;

/**
 * Represent an annotated field on a component instance.
 */
// Det skal bruges som plugin arkitektur.
// Hvordan virker det paa kryds af containere???
// Boer vel virke som en receiver. Hvis man levere
// Det man kan bruge den som en parameter til en metode...
// Virker aldrig med componennts der har TypeVariabels

// Hvordan virker det med generiske typer, f.eks. med List<String>
//// Tror ikke vi supportere generiske typer....

public final class InstanceOfHook<T> {

    /** The builder for the component type. */
    final ComponentModel.Builder builder;

    /** The annotated type. */
    private final Class<T> hookType;

    /** The annotated type. */
    private final Class<? extends T> actualType;

    /**
     * Creates a new hook instance.
     * 
     * @param builder
     *            the builder for the component type
     * @param hookType
     *            the annotated type
     * @param actualType
     *            the annotation value
     */
    InstanceOfHook(ComponentModel.Builder builder, Class<T> hookType, Class<? extends T> actualType) {
        this.builder = requireNonNull(builder);
        this.hookType = requireNonNull(hookType);
        this.actualType = requireNonNull(actualType);
    }

    /**
     * Returns the
     * 
     * @return the type we are hooked on
     */
    public Class<T> hookType() {
        return hookType;
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
    Class<? extends T> type() {
        return actualType;
    }
}

class MyHook {
    public void foo(InstanceOfHook<List<?>> h) {

        h.hookType();
    }
}