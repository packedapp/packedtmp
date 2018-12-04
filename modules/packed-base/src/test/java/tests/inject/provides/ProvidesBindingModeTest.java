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
package tests.inject.provides;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import app.packed.inject.BindingMode;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Provides;
import app.packed.inject.TypeLiteral;

/** Tests {@link Provides#bindingMode()}. */
public class ProvidesBindingModeTest {

    static Long L = 1L;

    static Integer P = 1;

    static Short S = 1;

    @Test
    public void staticFields() {
        StaticFields.test(c -> c.bind(new StaticFields()));
        StaticFields.test(c -> c.bind(StaticFields.class));
        StaticFields.test(c -> c.bind(new TypeLiteral<StaticFields>() {}));

        StaticFields.test(c -> c.bindLazy(StaticFields.class));
        StaticFields.test(c -> c.bindLazy(new TypeLiteral<StaticFields>() {}));

        StaticFields.test(c -> c.bindPrototype(StaticFields.class));
        StaticFields.test(c -> c.bindPrototype(new TypeLiteral<StaticFields>() {}));
    }

    @Test
    public void instanceFields() {
        InstanceFields.test(c -> c.bind(new InstanceFields()));
        InstanceFields.test(c -> c.bind(InstanceFields.class));
        InstanceFields.test(c -> c.bindLazy(InstanceFields.class));

        // Should fail
        InstanceFields.test(c -> c.bindPrototype(InstanceFields.class));
    }

    static class StaticFields {

        @Provides(bindingMode = BindingMode.LAZY)
        private static Long L = 1L;

        @Provides(bindingMode = BindingMode.PROTOTYPE)
        private static Integer P = 1;

        @Provides(bindingMode = BindingMode.SINGLETON)
        private static Short S = 1;

        static void test(Consumer<? super InjectorConfiguration> configurator) {
            L = 1L;
            P = 1;
            S = 1;
            Injector i = Injector.of(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });
            L = 2L;
            S = 2;
            P = 2;

            assertThat(i.with(Short.class)).isEqualTo((short) 1);
            assertThat(i.with(Long.class)).isEqualTo(2L);
            assertThat(i.with(Integer.class)).isEqualTo(2);
            L = 3L;
            S = 3;
            P = 3;
            assertThat(i.with(Short.class)).isEqualTo((short) 1);
            assertThat(i.with(Long.class)).isEqualTo(2L);
            assertThat(i.with(Integer.class)).isEqualTo(3);
        }
    }

    static class InstanceFields {

        @Provides(bindingMode = BindingMode.LAZY)
        Long l = 1L;

        @Provides(bindingMode = BindingMode.PROTOTYPE)
        Integer p = 1;

        @Provides(bindingMode = BindingMode.SINGLETON)
        Short S = 1;

        static void test(Consumer<? super InjectorConfiguration> configurator) {
            Injector i = Injector.of(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });
            InstanceFields f = i.with(InstanceFields.class);
            f.l = 2L;
            f.S = 2;
            f.p = 2;

            assertThat(i.with(Short.class)).isEqualTo((short) 1);
            assertThat(i.with(Long.class)).isEqualTo(2L);
            assertThat(i.with(Integer.class)).isEqualTo(2);
            f.l = 3L;
            f.S = 3;
            f.p = 3;
            assertThat(i.with(Short.class)).isEqualTo((short) 1);
            assertThat(i.with(Long.class)).isEqualTo(2L);
            assertThat(i.with(Integer.class)).isEqualTo(3);
        }
    }

    static class BindingModeMethods {

        @Provides(bindingMode = BindingMode.PROTOTYPE)
        static Integer P = 1;

        @Provides(bindingMode = BindingMode.SINGLETON)
        static Short S = 1;

        @Provides(bindingMode = BindingMode.LAZY)
        public static Long l() {
            return L;
        }

    }
}
