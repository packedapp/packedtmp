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
package packed.internal.component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import app.packed.component.App;
import app.packed.component.Image;
import app.packed.container.BaseBundle;
import app.packed.container.ComponentLinked;
import app.packed.container.Extension;
import app.packed.container.ExtensionSetup;

/**
 *
 */
public class Z4 extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        install(Doo.class);
        use(E.class);
        link(new MyChild());
        link(new MyChild());
        link(new MyChild());
        link(new MyChild());
        System.out.println("Bye");
    }

    public static void main(String[] args) {
        Image<App> img = App.imageOf(new Z4());
        img.stream().forEach(e -> System.out.println(e.path()));
    }

    static class MyChild extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            use(E.class);
            int ni = ThreadLocalRandom.current().nextInt(10);
            System.out.println(ni);
            if (ni != 0) {
                System.out.println("LINKING");
                link(new MyChild());
            }
        }
    }

    public static class Doo {
        public Doo() {}
    }

    static AtomicInteger i = new AtomicInteger();

    public static class E extends Extension {

        final int ai = i.getAndIncrement();

        @ComponentLinked
        public void linked(E child) {
            System.out.println("Linked child " + child);
        }

        @Override
        protected void add() {
            System.out.println(ai + "E-ADDED");
        }

        @Override
        protected void complete() {
            System.out.println(ai + "E-Complete");
        }

        @Override
        protected void preChildContainers() {
            System.out.println(ai + "E-PreChildContainers");
        }
    }

    @ExtensionSetup(dependencies = E.class)
    public static class F extends Extension {

        @Override
        protected void add() {
            System.out.println("F-ADDED");
        }

        @Override
        protected void complete() {
            System.out.println("F-Complete");
        }

        @Override
        protected void preChildContainers() {
            System.out.println("F-PreChildContainers");
            useOld(E.class);
        }
    }
}
