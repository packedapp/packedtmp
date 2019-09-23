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
package xpacked.internal.container.extension.a;

import app.packed.app.App;
import app.packed.app.AppBundle;
import app.packed.container.BaseBundle;
import app.packed.container.Bundle;
import app.packed.service.Factory;
import app.packed.service.Inject;
import app.packed.service.Injector;
import app.packed.service.ServiceContract;

/**
 *
 */
public class TestIt extends AppBundle {

    static Bundle b() {
        return new BaseBundle() {
            @Override
            protected void configure() {
                provideInstance("foob");
                provideInstance(-123L);
                provideInstance((short) -123L);
                exportAll();
            }
        };
    }

    static final Injector INJ = Injector.configure(c -> {
        c.provideInstance("foo123");
        c.provideInstance(123L);
    });

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        System.out.println(ServiceContract.of(b()));

        exportAll(); // <- we don't want this config site.... Man this is annoying

        Factory<?> ff = Factory.ofInstance("122323323").mapTo(Integer.class, e -> e.length());
        provide(Doo.class);
        provide(ff);

        // Wirelet w1 = ServiceWirelets.extractUpstream(String.class, Short.class, s -> (short) s.length());

        // provideAll(INJ, peekUpstream(e -> System.out.println("Adding " + e.key())), w1, peekUpstream(e ->
        // System.out.println("Importing " + e.key())));

    }

    public static void main(String[] args) {
        try (App a = App.of(new TestIt() /* , peekUpstream(e -> {})) */)) {
            System.out.println("");

            a.injector().services().forEach(e -> System.out.println(e));

            System.out.println("MAH");
            // System.out.println(a.use(Doo.class).foo);
        }
    }

    public static class Doo {

        @Inject
        public String foo;
    }
}
