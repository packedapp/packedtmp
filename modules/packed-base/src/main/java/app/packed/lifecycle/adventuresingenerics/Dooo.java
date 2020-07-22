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
package app.packed.lifecycle.adventuresingenerics;

import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.container.BaseBundle;
import app.packed.inject.Composite;
import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.Factory1;
import app.packed.inject.Factory2;
import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 *
 */
public class Dooo extends BaseBundle {

    <T> void install(Class<T> type, Supplier<T> s) {

    }

    static <I, R> void install(FN1<I, R> s) {

    }

    void install(Supplier<?> s) {

    }

    static <I, A> void install(Function<I, A> s) {
        TypeVariableExtractor tv0 = TypeVariableExtractor.of(Function.class, 0);
        System.out.println(tv0);

        System.out.println(s.getClass());

        System.out.println(tv0.extract(s.getClass()));

    }
    // Maaske skal vi sige at Object ikke er valid dependency...

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        install((Integer d) -> 4);
        install(Dooo::foo);

        Factory<Integer> fi1 = new Factory1<>((Integer i) -> 3) {};

        Factory<Integer> fi11 = new Factory1<Integer, Integer>(i -> 3) {};

        Factory<Integer> fi2 = new Factory2<>((Integer i, @Composite Dooo i2) -> 3) {};

        TypeVariableExtractor tv0 = TypeVariableExtractor.of(Factory1.class, 0);
        TypeVariableExtractor tv1 = TypeVariableExtractor.of(Factory1.class, 1);

        System.out.println(tv0.extract(fi1.getClass()));
        System.out.println(tv1.extract(fi1.getClass()));
    }

    static Integer foo(Integer ii) {
        return ii;
    }

    static void ddd(Integer i) {

    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        install(() -> 3);

        install(Integer.class, () -> 3);
        install(new Factory0<>(() -> 3) {});
        installConstant(3);

        install((Integer i) -> 3);

        install(new FN1<>((Integer i) -> 3) {});

        install(new Factory1<>((Integer i) -> 3) {});
        install(new Factory1<Integer, Integer>(i -> 3) {});
    }

    static abstract class FN1<T, R> {
        FN1(Function<T, R> f) {}
    }
}
