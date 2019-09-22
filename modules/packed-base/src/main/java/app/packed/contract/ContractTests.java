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
package app.packed.contract;

import app.packed.service.Injector;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceWirelets;

/**
 *
 */
public class ContractTests {
    public static void main(String[] args) {
        ServiceContract ic = ServiceContract.of(c -> {
            c.addProvides(String.class);
            c.addProvides(Long.class);
        });

        System.out.println(ContractSet.of(ic));
        System.out.println();
        Injector i = Injector.configure(c -> {
            c.provideInstance("Foo");
            c.provideInstance(3L);
        });

        System.out.println(i.contract());

        Injector i2 = i.spawn(ServiceWirelets.provide(4), ServiceWirelets.provide(99L));
        System.out.println();
        System.out.println(i2.contract());

        System.out.println(i2.use(Integer.class));
        System.out.println(i2.use(Long.class));
        // json
        // toString
    }
}
