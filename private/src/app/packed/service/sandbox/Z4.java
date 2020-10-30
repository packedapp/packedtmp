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
package app.packed.service.sandbox;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import app.packed.bundle.BaseAssembly;
import app.packed.bundle.ConnectExtensions;
import app.packed.bundle.Extension;
import app.packed.bundle.ExtensionSetup;
import app.packed.component.App;
import app.packed.component.Image;

/**
 *
 */
public class Z4 extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Doo.class);
        use(E.class);
        link(new MyChild(20));
        link(new MyChild(20));
        link(new MyChild(6));
        link(new MyChild(6));
        System.out.println("Bye");
    }

    public static void main(String[] args) throws InterruptedException {
        long s = System.currentTimeMillis();
        Image<App> img = App.imageOf(new Z4());
        System.out.println(System.currentTimeMillis() - s);
        System.out.println(img.stream().count());
        Thread.sleep(10000);
    }

    static class MyChild extends BaseAssembly {

        final int maxDepth;

        MyChild(int maxDepth) {
            this.maxDepth = maxDepth;
        }

        /** {@inheritDoc} */
        @Override
        protected void build() {
            use(E.class);
            if (maxDepth > 0) {
                int ni = ThreadLocalRandom.current().nextInt(6);
                if (ni != 0) {
                    link(new MyChild(maxDepth - 1));
                }
                ni = ThreadLocalRandom.current().nextInt(6);
                if (ni != 0) {
                    link(new MyChild(maxDepth - 1));
                }
            }
        }
    }

    public static class Doo {
        public Doo() {}
    }

    static AtomicInteger i = new AtomicInteger();

    public static class E extends Extension {

        final int ai = i.getAndIncrement();

        @ConnectExtensions
        public void linked(E child) {
            // System.out.println("Linked child " + child);
        }

        @Override
        protected void add() {
            // System.out.println(ai + "E-ADDED");
        }

        @Override
        protected void complete() {
            // System.out.println(ai + "E-Complete");
        }

        @Override
        protected void preChildContainers() {
            // System.out.println(ai + "E-PreChildContainers");
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
