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
package packed.internal.service.buildtime.service;

import app.packed.container.BaseBundle;
import app.packed.inject.Provide;
import app.packed.service.Injector;
import app.packed.service.ServiceContract;

/**
 *
 */
public class Z1 extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        installInstance(123);
        // export(Integer.class);
        // provide(Dx.class);
        // exportAll();
    }

    public static void main(String[] args) {
        System.out.println(ServiceContract.of(new Z1()));
        System.out.println("");
        Injector inj = Injector.create(new Z1());
        Object ii = inj.use(Integer.class);
        System.out.println(ii.getClass() + " " + ii);
        System.out.println(inj);
        System.out.println("BYE");
    }

    public static class XX {
        @Provide(constant = true)
        long foo = 23;
    }

    public static class Dx {
        public Dx(XX x) {

        }
    }
}
