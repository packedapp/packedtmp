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
package app.packed.container.extension;

/**
 * Represent an annotated field on a component instance.
 */
// Det skal bruges som plugin arkitektur.
// Hvordan virker det paa kryds af containere???
// Hvordan virker det f.eks. med List<String>
// Boer vel virke som en receiver. Hvis man levere
// Det man kan bruge den som en parameter til en metode...
// Virker aldrig med componennts der har TypeVariabels

public interface InstanceOfHook<T> {

    /**
     * Returns the
     * 
     * @return the type we are hooked on
     */
    Class<T> hookType(); // <- TypeLiteral

    /**
     * Returns the instance.
     *
     * @return the instance
     */
    // Den virker jo ikke.... Skal ogsaa have noget applicator noget.
    HookApplicator<T> applicator();

    /**
     * Returns the actual type (assignable to T).
     * 
     * @return the actual type
     */
    // Syntes det er rimeligt at klassen er til raadighed,
    // Den er det jo paa runtime
    Class<? extends T> type();
}
