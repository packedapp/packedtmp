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
package a;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;

import app.packed.container.BaseBundle;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.Provide;
import app.packed.util.Key;
import app.packed.util.Qualifier;

/**
 *
 */
public class ITest extends BaseBundle {
    public static void main(String[] args) {
        Injector.of(new ITest());

        Injector.configure(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(new Stuff());
            c.provide(new Stuff()).as(new Key<@QQ Stuff>() {});
            c.provide(new Stuff()).as(new Key<@QQ Stuff>() {});

            c.provide(new XXX());
            x(c);
        });
    }

    @Override
    public void configure() {
        provide(new Stuff());
        provide(new Stuff()).as(new Key<@QQ Stuff>() {});
        provide(new Stuff()).as(new Key<@QQ Stuff>() {});

        provide(new XXX());
    }

    public static void x(InjectorConfigurator ic) {
        ic.provide(new Stuff());
    }

    public static class Stuff {}

    public static class XXX {

        @Provide
        public Stuff pro(String nas) {
            throw new UnsupportedOperationException();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER })
    @Qualifier
    public @interface QQ {
        String value() default "X";
    }
}
