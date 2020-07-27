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
package app.packed.component.sourced;

/**
 *
 */
public class IF {

}

interface CC<T> {}

interface Driv<T, X extends CC<T>> {

    X install(T instance);
}

class DD {

    <T, X extends CC<T>> X instant(Driv<T, X> driv, T instance) {
        return driv.install(instance);
    }

    <T> Driv<T, MyC<T>> foo() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    void dfoo() {
        MyC<String> instant = instant(foo(), "hejhej");

        MyC<Integer> instant1 = instant(foo(), 123);
    }
}

interface MyC<T> extends CC<T> {

}

interface IC {}
// Driv<T, SomeD<T>>