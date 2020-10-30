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
package packed.internal.lifecycle.old;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import app.packed.base.Named;
import app.packed.component.App;
import app.packed.cube.BaseBundle;
import app.packed.inject.Provide;
import app.packed.sidecar.ActivateMethodSidecar;
import app.packed.sidecar.Invoker;
import app.packed.sidecar.MethodSidecar;
import app.packed.statemachine.OnInitialize;

/**
 *
 */
public class Foo extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("asdasdasd");
        provide(MyComp.class);
        System.out.println(extensions());
    }

    public static void main(String[] args) throws InterruptedException {
        App.of(new Foo());
    }

    @Target(ElementType.METHOD)
    @Retention(RUNTIME)
    @ActivateMethodSidecar(allowInvoke = true, sidecar = TestIt.class)
    public @interface Hej {

    }

    public static class MyComp {

        public MyComp(String s) {
            System.out.println(s);
        }

        @Hej
        public void foo(@Named("A") Long ldt, @Named("B") Long ldt2, @Named("A") Long ldt3, String sss) {
            System.out.println("Invoking because of @HEJ " + ldt + "   " + ldt2 + "  " + ldt3);
        }
    }
}

class TestIt extends MethodSidecar {

    static final AtomicLong al = new AtomicLong();

    @Override
    protected void configure() {
        provideInvoker();
        if (method().isDefault()) {
            disable();
        }
        attach(method().getName());
        System.out.println("CONF");
    }

    @OnInitialize
    public void foo(Invoker<?> i) throws Throwable {
        i.call();
    }

    @Provide
    @Named("A")
    public static Long l() {
        return al.incrementAndGet();
    }

    @Provide
    @Named("B")
    public static Long ld() {
        return al.decrementAndGet();
    }

    @Provide
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
