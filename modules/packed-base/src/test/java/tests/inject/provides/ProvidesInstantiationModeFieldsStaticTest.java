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

import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.InstantiationMode;
import app.packed.inject.Provides;
import app.packed.util.TypeLiteral;

/**
 * Tests {@link Provides#instantionMode()} on static fields. In general we do not need to create an instance of the
 * parent if we have static {@link Provides} fields. Unlike for instance fields.
 */
public class ProvidesInstantiationModeFieldsStaticTest {

    /** Tests default {@link Provides#instantionMode()} on static fields on components. */
    @Test
    public void install() {
        // MixedFieldsInstantiable.test(c -> c.install(new MixedFieldsInstantiable()));
        MixedFieldsInstantiable.test(c -> c.bind(MixedFieldsInstantiable.class));
        MixedFieldsInstantiable.test(c -> c.bind(Factory.findInjectable(MixedFieldsInstantiable.class)));
        MixedFieldsInstantiable.test(c -> c.bind(new TypeLiteral<MixedFieldsInstantiable>() {}));
    }

    /** Tests default {@link Provides#instantionMode()} on static fields. */
    @Test
    public void bindSingleton() {
        MixedFieldsInstantiable.test(c -> c.bind(new MixedFieldsInstantiable()));
        MixedFieldsInstantiable.test(c -> c.bind(MixedFieldsInstantiable.class));
        MixedFieldsInstantiable.test(c -> c.bind(Factory.findInjectable(MixedFieldsInstantiable.class)));
        MixedFieldsInstantiable.test(c -> c.bind(new TypeLiteral<MixedFieldsInstantiable>() {}));
    }

    /** Tests lazy {@link Provides#instantionMode()} on static fields. */
    @Test
    public void bindLazy() {
        MixedFieldsNoInstantiation.test(c -> c.bindLazy(MixedFieldsNoInstantiation.class));
        MixedFieldsNoInstantiation.test(c -> c.bindLazy(Factory.findInjectable(MixedFieldsNoInstantiation.class)));
        MixedFieldsNoInstantiation.test(c -> c.bindLazy(new TypeLiteral<MixedFieldsNoInstantiation>() {}));
    }

    /** Tests prototype {@link Provides#instantionMode()} on static fields. */
    @Test
    public void bindPrototype() {
        MixedFieldsNoInstantiation.test(c -> c.bindPrototype(MixedFieldsNoInstantiation.class));
        MixedFieldsNoInstantiation.test(c -> c.bindPrototype(Factory.findInjectable(MixedFieldsNoInstantiation.class)));
        MixedFieldsNoInstantiation.test(c -> c.bindPrototype(new TypeLiteral<MixedFieldsNoInstantiation>() {}));
    }

    /** A helper class that can be instantiated. */
    static class MixedFieldsInstantiable {

        @Provides(instantionMode = InstantiationMode.LAZY)
        private static Long L;

        @Provides(instantionMode = InstantiationMode.PROTOTYPE)
        private static Integer P;

        @Provides(instantionMode = InstantiationMode.SINGLETON)
        private static Short S;

        MixedFieldsInstantiable() {
            assertThat(L).isEqualByComparingTo(1L);
            assertThat(P).isEqualByComparingTo(1);
            assertThat(S).isEqualByComparingTo((short) 1);
        }

        static void test(Consumer<? super InjectorConfiguration> configurator) {
            L = 1L;
            P = 1;
            S = 1;
            Injector i = Injector.of(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });
            assertThat(i.with(MixedFieldsInstantiable.class)).isNotNull();
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

    /**
     * A helper class that should never be instantiated. Because we can read the value of the fields without an instance of
     * BindStaticNoInstantiation.
     */
    static class MixedFieldsNoInstantiation {

        @Provides(instantionMode = InstantiationMode.LAZY)
        private static Long L;

        @Provides(instantionMode = InstantiationMode.PROTOTYPE)
        private static Integer P;

        @Provides(instantionMode = InstantiationMode.SINGLETON)
        private static Short S;

        public MixedFieldsNoInstantiation() {
            throw new AssertionError("Cannot instantiate");
        }

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
}
