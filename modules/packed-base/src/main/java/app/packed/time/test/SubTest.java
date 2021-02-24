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
package app.packed.time.test;

import java.util.function.Consumer;

import app.packed.cli.Main;
import app.packed.component.App;
import app.packed.component.Composer;
import app.packed.component.Wirelet;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.inject.ServiceComponentConfiguration;

/**
 *
 */
public class SubTest {
    public static void main(String[] args) {
        Main.run(new BaseAssembly() {

            @Override
            protected void build() {
                use(MyExt.class).l = 123;
                use(MyExtUse.class).foo();
            }
        });
    }

    public static final class AppComposer extends Composer<ContainerConfiguration> {

        /**
         * @param configuration
         */
        protected AppComposer(ContainerConfiguration configuration) {
            super(configuration);
        }

        public <T> ServiceComponentConfiguration<T> installInstance(T instance) {
            throw new UnsupportedOperationException();
        }

        static App configure(Consumer<? super AppComposer> configurator, Wirelet... wirelets) {

            AppComposer.configure(a -> {
                a.installInstance("foo");
                a.installInstance("foccco");
            });
            throw new UnsupportedOperationException();
        }

    }

    public static final class MyExt extends Extension {
        private long l;

        MyExt() {}

        public class Sub extends Subtension {

            public void nice() {
                System.out.println("Nix " + l);
            }
        }
    }

    public static final class MyExtUse extends Extension {
        static {
            $addDependency(MyExt.class);
        }

        MyExtUse() {}

        void foo() {
            use(MyExt.Sub.class).nice();
        }
    }

}
