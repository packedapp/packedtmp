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
package app.packed.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.app.App;

/**
 *
 */
public interface Executable<T> {
    void run(T t) throws Throwable;
}

class D {

    public static void main(Consumer<App> e) {

    }

    public static <T> T mainTask(Function<App, T> e) {
        throw new UnsupportedOperationException();
    }
}

class E {

    public static void main(String[] args) {
        D.main(a -> a.with(List.class).clear());

        int size = D.mainTask(a -> a.with(List.class).size());

        System.out.println(size);
    }
}
