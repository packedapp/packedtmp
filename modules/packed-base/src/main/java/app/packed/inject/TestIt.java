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
 *
 */
public class TestIt {

    @Provides
    public static String s = "ddd";

    @Provides(bindingMode = BindingMode.LAZY)
    public static Long ff() {
        System.out.println("J");
        return System.nanoTime();
    }

    public static void main(String[] args) {
        Injector i = Injector.of(c -> {
            c.bind(new TestIt());
            c.bind(CC.class);
        });
        i.services().forEach(e -> {
            System.out.println(e);
        });
        System.out.println(i.with(String.class));
        System.out.println(i.with(Long.class));
        System.out.println(i.with(Long.class));
    }

    public static class CC {
        public CC(Long l) {
            System.out.println("XXXXX " + l);
        }
    }
}
