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

import app.packed.base.Nullable;
import app.packed.container.BaseBundle;
import app.packed.container.Wirelet;
import app.packed.inject.Factory1;

/**
 *
 */
public class Dooo2 extends BaseBundle {

    void install(Supplier<?> s) {

    }

    static <I, A> void install(Function<I, A> s) {
//        TypeVariableExtractor tv0 = TypeVariableExtractor.of(Function.class, 0);
//        System.out.println(tv0);
//
//        System.out.println(s.getClass());
//
//        System.out.println(tv0.extract(s.getClass()));

    }
    // Maaske skal vi sige at Object ikke er valid dependency...

//    public static void main(String[] args) {
//        install((Integer d) -> 4);
//        install(Dooo2::foo);
//        Factory<Integer> fi1 = new Factory1<>((Integer i) -> 3) {};
//    }
//
//    static Integer foo(Integer ii) {
//        return ii;
//    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        install(() -> 3L);
        install((@Nullable Wirelet i) -> 3);
        install(new Factory1<>((Integer i) -> 3) {});
    }

}
