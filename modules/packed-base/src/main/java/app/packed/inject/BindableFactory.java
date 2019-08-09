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
package app.packed.inject;

/**
 * A mutable factory where you can bind dependencies. For example,
 */
public final class BindableFactory<T> extends Factory<T> {

    /**
     * @param factory
     */
    BindableFactory(Factory<T> factory) {
        super(factory.factory);
    }

    public Factory<T> immutable() {
        // Vi bliver noedt til at lave en copy.... Ellers kan man jo binde videre...
        // Eller ogsaa skal man bare ikke kunne aendre i den oprindelige mere????

        // Vi skal
        return new Factory<>(factory);
    }

    // lidt diskussion om vi skal override nogle af de factory metoder. Men syntes bare man automatisk laver
    // en immutable factory, naar man starter med det....
}
