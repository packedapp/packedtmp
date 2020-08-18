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

import app.packed.container.ContainerDescriptor;
import app.packed.container.BaseBundle;
import app.packed.service.Injector;
import app.packed.service.ServiceDescriptor;

/**
 *
 */
public class BTest {

    public static void main(String[] args) {
        Injector i = Injector.of(new MyBundle());

        i.getDescriptor(PrivateImplementation.class).get().configSite().print();

        System.out.println(ContainerDescriptor.of(new MyBundle()));
        System.out.println(String.class.getModule().getDescriptor());

        i = Injector.configure(c -> {
            c.link(new MyBundle());
            c.link(new MyBundle4());
            c.provideInstance("123");
        });
        System.out.println("");
        for (ServiceDescriptor d : i.services().collect(Collectors.toList())) {
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
            provideConstant(123L);
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
