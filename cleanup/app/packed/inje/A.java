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
package app.packed.inje;

import app.packed.app.App;
import app.packed.app.Host;
import app.packed.container.ComponentServiceConfiguration;

/**
 *
 */
public class A {

    public static void main(String[] args) {
        // Injector i = Injector.of(c -> {
        // c.lookup(MethodHandles.lookup());
        // c.provide(new Factory0<>(System::nanoTime) {});
        // c.provide(new Factory0<>(System::nanoTime) {}.mapTo(String::valueOf, String.class));
        // });
        // System.out.println(i.with(Long.class));
        // System.out.println(i.with(String.class));

        App.of(c -> {
            Host.install(c);
        });

    }

    static <Conf, InstanceType, T extends Role<Conf, InstanceType>> Conf register(Class<T> type, InstanceType instance) {
        throw new UnsupportedOperationException();
    }

    interface Role<Conf, InstanceType> {

    }

    class D<InstanceType> implements Role<ComponentServiceConfiguration<InstanceType>, InstanceType> {

    }

    class E implements Role<ComponentServiceConfiguration<String>, String> {

    }

    // static class Host {
    //
    // public static String install(ComponentInstaller installer, String name) {
    // throw new UnsupportedOperationException();
    // }
    // }
}
