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
package packed.internal.hooks.variable;

import app.packed.application.ApplicationMirror;
import app.packed.application.entrypoint.Main;
import app.packed.application.programs.SomeApp;
import app.packed.bean.BeanExtension;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;

/**
 *
 */
public class HelloWorldAssembly extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        use(BeanExtension.class).install(HelloWorld.class);
        provide(HelloWorld.class);
    }

    public static void main(String[] args) {
        SomeApp.run(new HelloWorldAssembly());

        ApplicationMirror.of(new HelloWorldAssembly()).components().forEach(c -> System.out.println(c.path()));

        ApplicationMirror.of(new HelloWorldAssembly()).forEachComponent(c -> System.out.println(c.path()));

        ContainerMirror.of(new HelloWorldAssembly()).forEachComponent(c -> System.out.println(c.path()));

        System.out.println();
        SomeApp.driver().print(new HelloWorldAssembly());
    }

    public static class HelloWorld {

        public HelloWorld() {
            System.out.println("NEW HELL");
        }

        @Main
        public static void execute() {
            System.out.println("HelloWorld");
        }
    }
}
