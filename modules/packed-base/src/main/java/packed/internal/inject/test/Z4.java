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
package packed.internal.inject.test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import app.packed.application.Program;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;

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
        
        for (;;) {
            long s = System.currentTimeMillis();
            Program.newImage(new Z4());
            System.out.println(System.currentTimeMillis() - s);
        }
        //Thread.sleep(10000);
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
        E() {}

        final int ai = i.getAndIncrement();

        public void linked(E child) {
            // System.out.println("Linked child " + child);
        }

        @Override
        protected void onNew() {
            // System.out.println(ai + "E-ADDED");
        }

        @Override
        protected void onComplete() {
            // System.out.println(ai + "E-Complete");
        }

        @Override
        protected void onPreChildren() {
            // System.out.println(ai + "E-PreChildContainers");
        }

        class Sub extends Subtension {

        }
    }

    public static class F extends Extension {
        F() {}

        static {
            $dependsOnOptionally(E.class);
        }

        @Override
        protected void onNew() {
            System.out.println("F-ADDED");
        }

        @Override
        protected void onComplete() {
            System.out.println("F-Complete");
        }

        @Override
        protected void onPreChildren() {
            System.out.println("F-PreChildContainers");
            use(E.Sub.class);
        }
    }
}
