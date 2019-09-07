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
package packed.internal.inject.util.stuff;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
public class TestIt {

    public static void main(String[] args) {
        TypeVariableExtractor tve = TypeVariableExtractor.of(Consumer.class);
        // System.out.println(tve.extract(Y.class));
        System.out.println(tve.extract(Z.class));
    }

    interface X<T> extends Consumer<T> {}

    interface Y extends Consumer<String> {}

    interface Z extends X<String> {}

    ////////// Led....
    interface ZZ<T> extends Function<T, String> {}

    interface YY<T> extends Function<String, T> {}

    interface WW extends ZZ<String>, YY<String> {}
}
