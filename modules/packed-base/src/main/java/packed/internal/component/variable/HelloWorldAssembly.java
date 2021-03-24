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
package packed.internal.component.variable;

import app.packed.application.Main;
import app.packed.container.BaseAssembly;
import app.packed.request.Compute;

/**
 *
 */
public class HelloWorldAssembly extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(HelloWorld.class);
    }

    public static void main(String[] args) {
        Main.run(new HelloWorldAssembly());
        Main.driver().analyze(new HelloWorldAssembly()).stream().forEach(c -> System.out.println(c.path()));
        System.out.println();
        Main.driver().print(new HelloWorldAssembly());
    }

    public static class HelloWorld {

        @Compute
        public static void execute() {
            System.out.println("HelloWorld");
        }
    }
}