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
package xackedinject;

import java.lang.invoke.MethodHandles;

import app.packed.inject.Injector;

/**
 *
 */
public class Example {

    Example() {}

    public static void main(String[] args) {
        Injector x1 = Injector.of(e -> e.provide(123));

        Injector i = Injector.of(e -> {
            e.lookup(MethodHandles.lookup());
            e.provide(Example.class);
            e.provide("foxxxx");
            e.wireInjector(x1);
        });

        System.out.println(i.with(String.class));
        System.out.println(i.with(Example.class));
        System.out.println(i.with(Integer.class));
    }
}
