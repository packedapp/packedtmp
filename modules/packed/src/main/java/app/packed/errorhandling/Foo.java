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
package app.packed.errorhandling;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.container.AssemblyMirror;
import app.packed.container.BaseAssembly;
import app.packed.entrypoint.Main;

/**
 *
 */
public class Foo extends BaseAssembly {
    final int size;

    Foo(int size) {
        this.size = size;
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {
        if (size == 10) {
            installInstance(new Fodo());
        }
        if (size > 0) {
            link(new Foo(size - 1));
            link(new Foo(size - 1));
        }
        provideInstance("asdasd");
    }

    public static void main(String[] args) {
        App.run(new Foo(10));
        long start = System.currentTimeMillis();
        ApplicationMirror am = App.mirrorOf(new Foo(10));
        System.out.println(System.currentTimeMillis() - start);
        // AssemblyMirror a = am.assembly();
        // print(0, a);
        System.out.println(am.lifetime());
    }

    static void print(int indent, AssemblyMirror am) {
        System.out.println(" ".repeat(indent * 2) + " " + am.assemblyClass());
        am.children().forEach(e -> print(indent + 1, e));
    }

    public static class Fodo {

        @Main
        public void hello() {
            System.out.println("Hello");
        }
    }
}
