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

import app.packed.cli.Main;
import app.packed.container.BaseBundle;
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
        use(F.class);
        link(new MyChild());
    }

    public static void main(String[] args) {
        Main.execute(new Z4());
    }

    static class MyChild extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            use(E.class);
        }

    }

    public static class Doo {
        public Doo() {}
    }

    public static class E extends Extension {

        @Override
        protected void add() {
            System.out.println("E-ADDED");
        }

        @Override
        protected void complete() {
            System.out.println("E-Complete");
        }

        @Override
        protected void preChildContainers() {
            System.out.println("E-PreChildContainers");
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
