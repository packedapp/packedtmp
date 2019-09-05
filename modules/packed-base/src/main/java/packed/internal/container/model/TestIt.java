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
package packed.internal.container.model;

import static app.packed.inject.UpstreamServiceWirelets.map;
import static app.packed.inject.UpstreamServiceWirelets.peek;

import app.packed.app.App;
import app.packed.app.AppBundle;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.Inject;
import app.packed.inject.Injector;
import app.packed.inject.UpstreamServiceWirelets;
import app.packed.util.Key;

/**
 *
 */
public class TestIt extends AppBundle {

    static final Injector INJ = Injector.configure(c -> {
        c.provide("foo123");
        c.provide(123L);
    });

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        exportAll();

        Factory<Integer> ff = Factory.ofInstance("122323323").mapTo(e -> e.length(), Integer.class);

        Wirelet w = map(Key.of(String.class), Key.of(Short.class), s -> (short) s.length());
        provideAll(INJ, peek(e -> System.out.println("Adding " + e.key())), w, peek(e -> System.out.println("Importing " + e.key())));

        provide(Doo.class);
        provide(ff);

    }

    public static void main(String[] args) {

        try (App a = App.of(new TestIt(), UpstreamServiceWirelets.peek(e -> {}))) {
            System.out.println("");
            a.injector().services().forEach(e -> System.out.println(e));

            System.out.println(a.injector().use(Short.class));

            // System.out.println(a.use(Doo.class).foo);
        }
    }

    public static class Doo {

        @Inject
        public String foo;
    }
}
