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
package app.packed.lifecycle2.fn;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.inject.Factory;
import app.packed.inject.Factory1;
import app.packed.service.ServiceExtension;

/**
 *
 */
public class Dsss {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Supplier<Integer> ss = () -> 3;

        Function<Integer, Integer> fi = i -> 3;

        Function<Integer, Integer> fix = (@Nullable var i) -> 3;

        Function<Integer, Integer> fit = (@Nullable Integer i) -> 3;

        Factory<Integer> fi1 = new Factory1<>((@Nullable var i) -> 3) {};

        Factory<Integer> fi2 = new Factory1<@Nullable Integer, Integer>(i -> 3) {};

        foo(fit);

        BiFunction<Integer, Integer, Integer> fi1123 = (var i, var y) -> i + y;
    }

    static Integer foo(Function<Integer, Integer> fi) {
        return fi.apply(123);
    }

    void foo(ServiceExtension e) {
        // e.breakCycle(new OP2<String, Integer>((s, i) -> s.compareTo(i.toString())) {});

        // e.breakCycle(Key.of(String.class), Key.of(Integer.class), (s, i) -> s.compareTo(i.toString()));

        // e.breakCycle(String.class, Integer.class, (s, i) -> s.compareTo(i.toString()));
    }
}
