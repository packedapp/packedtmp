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
package app.packed.lang;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.artifact.App;

/**
 *
 */
// Hvorfor tager run() en parameter????
interface ThrowingRunnable<T> {
    void run(T t) throws Throwable;
}

class Dx {

    public static void main(Consumer<App> e) {

    }

    public static <T> T mainTask(Function<App, T> e) {
        throw new UnsupportedOperationException();
    }
}

class Ex {

    public static void main(String[] args) {
        Dx.main(a -> a.use(List.class).clear());

        int size = Dx.mainTask(a -> a.use(List.class).size());

        System.out.println(size);
    }
}
