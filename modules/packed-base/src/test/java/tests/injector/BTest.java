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
package tests.injector;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import app.packed.config.ConfigSite;
import app.packed.cube.BaseBundle;
import app.packed.inject.Service;
import packed.internal.inject.service.sandbox.Injector;

/**
 *
 */
public class BTest {

    public static void main(String[] args) {
        Injector i = Injector.create(new MyBundle());

        i.find(PrivateImplementation.class).get().attribute(ConfigSite.ATTRIBUTE).print();

        System.out.println(String.class.getModule().getDescriptor());

        i = Injector.configure(c -> {
            c.link(new MyBundle());
            c.link(new MyBundle4());
            c.provideInstance("123");
        });
        System.out.println("");
        for (Service d : i.stream().collect(Collectors.toList())) {
            System.out.println(d);
        }

    }

    public static class MyBundle extends BaseBundle {

        @Override
        protected void configure() {
            super.lookup(MethodHandles.lookup());
            provide(Private.class);
            provide(PrivateImplementation.class);
        }
    }

    public static class MyBundle4 extends BaseBundle {

        @Override
        protected void configure() {
            lookup(MethodHandles.lookup());
            provideInstance(123L);
        }
    }

    interface PublicInterface {}

    static class Private {}

    static class PrivateImplementation implements PublicInterface {
        PrivateImplementation(Private impl) {}
    }

    // kun internt (bind)
    // internt + externs (
    // kun externt (Det kan man ikke, med mindre we exposePrototype, ect hvilket bliver noget rod)

}
