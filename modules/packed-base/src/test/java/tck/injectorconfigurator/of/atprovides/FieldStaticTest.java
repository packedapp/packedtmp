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
package tck.injectorconfigurator.of.atprovides;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import app.packed.base.TypeLiteral;
import app.packed.inject.Factory;
import app.packed.service.Injector;
import app.packed.service.InjectorAssembler;
import app.packed.service.ServiceMode;
import app.packed.service.Provide;

/**
 * Tests {@link Provide#instantionMode()} on static fields. In general we do not need to create an instance of the
 * parent if we have static {@link Provide} fields. Unlike for instance fields.
 */
public class FieldStaticTest {

    /** Tests default {@link Provide#instantionMode()} on static fields. */
    @Test
    public void provide() {
        MixedFieldsInstantiable.test(c -> c.provideInstance(new MixedFieldsInstantiable()));
        MixedFieldsInstantiable.test(c -> c.provide(MixedFieldsInstantiable.class));
        MixedFieldsInstantiable.test(c -> c.provide(Factory.find(MixedFieldsInstantiable.class)));
        MixedFieldsInstantiable.test(c -> c.provide(Factory.find(new TypeLiteral<MixedFieldsInstantiable>() {})));
    }

    // /** Tests lazy {@link Provide#instantionMode()} on static fields. */
    // @Test
    // public void provideLazy() {
    // MixedFieldsNoInstantiation.test(c -> c.provide(MixedFieldsNoInstantiation.class).lazy());
    // MixedFieldsNoInstantiation.test(c -> c.provide(Factory.findInjectable(MixedFieldsNoInstantiation.class)).lazy());
    // MixedFieldsNoInstantiation.test(c -> c.provide(Factory.findInjectable(new TypeLiteral<MixedFieldsNoInstantiation>()
    // {})).lazy());
    // }

    /** Tests prototype {@link Provide#instantionMode()} on static fields. */
    @Test
    public void providePrototype() {
        MixedFieldsNoInstantiation.test(c -> c.provide(MixedFieldsNoInstantiation.class).prototype());
        MixedFieldsNoInstantiation.test(c -> c.provide(Factory.find(MixedFieldsNoInstantiation.class)).prototype());
        MixedFieldsNoInstantiation.test(c -> c.provide(Factory.find(new TypeLiteral<MixedFieldsNoInstantiation>() {})).prototype());
    }

    /** A helper class that can be instantiated. */
    static class MixedFieldsInstantiable {
        //
        // @Provide(instantionMode = InstantiationMode.LAZY)
        // private static Long L;

        @Provide(instantionMode = ServiceMode.PROTOTYPE)
        private static Integer P;

        @Provide(instantionMode = ServiceMode.SINGLETON)
        private static Short S;

        MixedFieldsInstantiable() {
            // assertThat(L).isEqualByComparingTo(1L);
            assertThat(P).isEqualByComparingTo(1);
            assertThat(S).isEqualByComparingTo((short) 1);
        }

        static void test(Consumer<? super InjectorAssembler> configurator) {
            // L = 1L;
            P = 1;
            S = 1;
            Injector i = Injector.configure(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });
            assertThat(i.use(MixedFieldsInstantiable.class)).isNotNull();
            // L = 2L;
            S = 2;
            P = 2;

            assertThat(i.use(Short.class)).isEqualTo((short) 1);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(2);
            // L = 3L;
            S = 3;
            P = 3;
            assertThat(i.use(Short.class)).isEqualTo((short) 1);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(3);
        }
    }

    /**
     * A helper class that should never be instantiated. Because we can read the value of the fields without an instance of
     * BindStaticNoInstantiation.
     */
    static class MixedFieldsNoInstantiation {

        // @Provide(instantionMode = InstantiationMode.LAZY)
        // private static Long L;

        @Provide(instantionMode = ServiceMode.PROTOTYPE)
        private static Integer P;

        @Provide(instantionMode = ServiceMode.SINGLETON)
        private static Short S;

        public MixedFieldsNoInstantiation() {
            throw new AssertionError("Cannot instantiate");
        }

        static void test(Consumer<? super InjectorAssembler> configurator) {
            // L = 1L;
            P = 1;
            S = 1;
            Injector i = Injector.configure(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });

            // L = 2L;
            S = 2;
            P = 2;

            assertThat(i.use(Short.class)).isEqualTo((short) 1);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(2);
            // L = 3L;
            S = 3;
            P = 3;
            assertThat(i.use(Short.class)).isEqualTo((short) 1);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(3);
        }
    }
}
