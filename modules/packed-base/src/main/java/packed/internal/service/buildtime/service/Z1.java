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
import app.packed.inject.Factory0;
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
        // provide(Dx.class);
//        provide(new Factory0<>(() -> {
//            System.out.println("New");
//            return 123123L;
//        }) {});

        // New Foo()
        // Old MethodHandle(Foo)DxString new TYPE (Region)Object

        // provide(Foo.class);
        provide(new Factory0<>(() -> new Foo()) {});

        // providePrototype(new Factory0() {});

        install(DxString.class);

        export(Integer.class);
        export(Foo.class);
        export(Double.class);
    }

    public static void main(String[] args) {
        long now = System.currentTimeMillis();
        Injector inj = Injector.create(new Z1());
        Integer ii = inj.use(Integer.class);
        System.out.println(inj.use(Foo.class));
        System.out.println(inj.use(Foo.class));
        System.out.println(inj.use(Double.class));
        System.out.println(ii.getClass() + " " + ii);
        // System.out.println(inj.use(Dx.class).x);
        System.out.println("BYE");
        System.out.println(System.currentTimeMillis() - now);
    }

    public static class Foo {
        public Foo() {
            System.out.println("New Foo()");
        }
    }

    public static class NoDep {

        @Provide(constant = true)
        public static Double d = 444D;

        public NoDep() {
            System.out.println("Instantiated");
        }

        @Provide(constant = true)
        public static String s() {
            return "adsasd";
        }

//        @Provide
        public Long d2s() {
            return 121231231233L;
        }
    }

    public static class Dx {
        final Integer x;

        public Dx(Integer x) {
            this.x = requireNonNull(x);
        }
    }

    public static class DxString {
        public DxString(/* Dx dx, */ Foo i) {
            System.out.println("Instantiated " + i);
        }
    }
}
