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
package packed.internal.inject.buildnodes;

import app.packed.bundle.InjectorBundle;
import app.packed.inject.Injector;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.Letters.NeedsA;
import support.stubs.Letters.NeedsB;

/**
 *
 */
public class TestIt {

    public static void main(String[] args) {
        Injector i = Injector.of(c -> {
            c.bind(B.class);
            c.bind(NeedsB.class);
        });

        System.out.println(i.with(NeedsB.class));

        System.out.println("      ");
        InjectorBundle ib = new InjectorBundle() {
            @Override
            protected void configure() {
                bind(A.class);
                bind(NeedsA.class);
                importServices(i);

                expose(A.class);
                expose(NeedsA.class);
            }
        };

        Injector i2 = Injector.of(ib);
        System.out.println(i2.with(A.class));
        System.out.println();
        i.services().forEach(e -> System.out.println(e.getKey()));

    }
}
