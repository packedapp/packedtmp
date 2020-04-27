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

import app.packed.base.Nullable;

/**
 *
 */
public class Dddd {

    public static void main(String[] args) {
        Function<?, ?> s = (@Nullable Integer ii) -> 3;

        main(s);

        main(new Ddd());

        Function<Integer, Integer> ff = Dddd::foo;
        main(ff);
    }

    public static void main(Function<?, ?> s) {
        System.out.println("-----");
        System.out.println(s.getClass().getGenericInterfaces()[0]);

        for (var m : s.getClass().getDeclaredMethods()) {
            if (!m.isSynthetic()) {
                System.out.println(m.getGenericReturnType());
                System.out.println(m.getGenericParameterTypes()[0]);
                System.out.println(m.getAnnotations().length);
            }
        }
    }

    static <T> T foo(T ii) {
        return ii;
    }

    public static Byte tox(Byte b) {
        return b;
    }

    static class Ddd implements Function<Long, Long> {

        /** {@inheritDoc} */
        @Override
        public Long apply(Long t) {
            return 123L;
        }
    }
}
