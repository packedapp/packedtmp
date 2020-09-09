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

import static java.util.Objects.requireNonNull;

import app.packed.container.BaseBundle;
import app.packed.inject.Provide;
import app.packed.service.Injector;

/**
 *
 */
public class Z1 extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        provideInstance(123);
        provide(NoDep.class);
        export(Integer.class);
    }

    public static void main(String[] args) {
        long now = System.currentTimeMillis();
        Injector inj = Injector.create(new Z1());
        Integer ii = inj.use(Integer.class);
        System.out.println(ii.getClass() + " " + ii);
        // System.out.println(inj.use(Dx.class).x);
        System.out.println("BYE");
        System.out.println(System.currentTimeMillis() - now);
    }

    public static class NoDep {
        public NoDep() {
            System.out.println("Instantiated");
        }

        @Provide(constant = true)
        public String s() {
            return "adsasd";
        }
    }

    public static class Dx {
        final Integer x;

        public Dx(Integer x) {
            this.x = requireNonNull(x);
        }
    }

    public static class DxString {
        public DxString(Dx dx, Integer i) {
            System.out.println("Instantiated");
        }
    }
}
