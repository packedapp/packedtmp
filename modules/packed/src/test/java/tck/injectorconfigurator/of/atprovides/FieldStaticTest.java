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

import app.packed.operation.Op0;
import app.packed.service.ProvideService;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceLocator.Composer;

/**
 * Tests {@link ProvideService#constant()} on static fields. In general we do not need to create an instance of the
 * parent if we have static {@link ProvideService} fields. Unlike for instance fields.
 */
public class FieldStaticTest {

    /** Tests default {@link ProvideService#constant()} on static fields. */
    @Test
    public void provide() {
        MixedFieldsInstantiable.test(c -> c.provideInstance(new MixedFieldsInstantiable()));
        MixedFieldsInstantiable.test(c -> c.provide(MixedFieldsInstantiable.class));
        MixedFieldsInstantiable.test(c -> c.provide(new Op0<>(MixedFieldsInstantiable::new) {}));
    }

    /** Tests prototype {@link ProvideService#constant()} on static fields. */
    @Test
    public void providePrototype() {
        MixedFieldsNoInstantiation.test(c -> c.providePrototype(MixedFieldsNoInstantiation.class));
    }

    /** A helper class that can be instantiated. */
    public static class MixedFieldsInstantiable {
        //
        // @Provide(instantionMode = InstantiationMode.LAZY)
        // private static Long L;

        @ProvideService
        private static Integer P;

        @ProvideService
        private static Short S;

        public MixedFieldsInstantiable() {
            // assertThat(L).isEqualByComparingTo(1L);
            assertThat(P).isEqualByComparingTo(1);
            assertThat(S).isEqualByComparingTo((short) 1);
        }

        static void test(Consumer<? super Composer> configurator) {
            // L = 1L;
            P = 1;
            S = 1;
            ServiceLocator i = ServiceLocator.of(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });
            assertThat(i.use(MixedFieldsInstantiable.class)).isNotNull();
            // L = 2L;
            S = 2;
            P = 2;

            assertThat(i.use(Short.class)).isEqualTo((short) 2);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(2);
            // L = 3L;
            S = 3;
            P = 3;
            assertThat(i.use(Short.class)).isEqualTo((short) 3);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(3);
        }
    }

    /**
     * A helper class that should never be instantiated. Because we can read the value of the fields without an instance of
     * BindStaticNoInstantiation.
     */
    public static class MixedFieldsNoInstantiation {

        // @Provide(instantionMode = InstantiationMode.LAZY)
        // private static Long L;

        @ProvideService
        private static Integer P;

        @ProvideService
        private static Short S;

        public MixedFieldsNoInstantiation() {
            throw new AssertionError("Cannot instantiate");
        }

        static void test(Consumer<? super Composer> configurator) {
            // L = 1L;
            P = 1;
            S = 1;
            ServiceLocator i = ServiceLocator.of(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });

            // L = 2L;
            S = 2;
            P = 2;

            assertThat(i.use(Short.class)).isEqualTo((short) 2);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(2);
            // L = 3L;
            S = 3;
            P = 3;
            assertThat(i.use(Short.class)).isEqualTo((short) 3);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(3);
        }
    }
}
