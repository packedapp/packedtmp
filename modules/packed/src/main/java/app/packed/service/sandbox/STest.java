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

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.lifecycle.Inject;

/**
 *
 */
public class STest extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(A.class);
        provide(Foo.class).bindConstant(A.class, new A("BNEA"));
    }

    public static void main(String[] args) {
        App.run(new STest());
        IO.println("Bye");
    }

    public record Foo(A a) {
        public Foo(@ServiceResolver(order = ServiceProviderKind.NAMESPACE_SERVICE) A a) {
            this.a = a;
            IO.println(a);
        }
    }

    public record A(String v) {
        @Inject
        public A() {
            this("Container");
        }
    }
}
