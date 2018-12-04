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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.inject.BindingMode;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Provides;
import app.packed.inject.TypeLiteral;

/** Tests {@link Provides#bindingMode()} on fields. */
public class ProvidesBindingModeFieldsTest {

    /** Tests default {@link Provides#bindingMode()} on static fields. */
    @Test
    public void bindStaticFields() {
        BindStaticInstantiable.test(c -> c.bind(new BindStaticInstantiable()));
        BindStaticInstantiable.test(c -> c.bind(BindStaticInstantiable.class));
        BindStaticInstantiable.test(c -> c.bind(Factory.findInjectable(BindStaticInstantiable.class)));
        BindStaticInstantiable.test(c -> c.bind(new TypeLiteral<BindStaticInstantiable>() {}));
    }

    /** Tests lazy {@link Provides#bindingMode()} on static fields. */
    @Test
    public void bindLazyStaticFields() {
        BindStaticNoInstantiation.test(c -> c.bindLazy(BindStaticNoInstantiation.class));
        BindStaticNoInstantiation.test(c -> c.bindLazy(Factory.findInjectable(BindStaticNoInstantiation.class)));
        BindStaticNoInstantiation.test(c -> c.bindLazy(new TypeLiteral<BindStaticNoInstantiation>() {}));
    }

    /** Tests prototype {@link Provides#bindingMode()} on static fields. */
    @Test
    public void bindPrototypeStaticFields() {
        BindStaticNoInstantiation.test(c -> c.bindPrototype(BindStaticNoInstantiation.class));
        BindStaticNoInstantiation.test(c -> c.bindPrototype(Factory.findInjectable(BindStaticNoInstantiation.class)));
        BindStaticNoInstantiation.test(c -> c.bindPrototype(new TypeLiteral<BindStaticNoInstantiation>() {}));
    }

    /** Tests default {@link Provides#bindingMode()} on instance fields. */
    @Test
    public void bindInstanceFields() {
        InstanceFields.test(c -> c.bind(new InstanceFields()));
        InstanceFields.test(c -> c.bind(InstanceFields.class));
        InstanceFields.test(c -> c.bind(Factory.findInjectable(InstanceFields.class)));
        InstanceFields.test(c -> c.bind(new TypeLiteral<InstanceFields>() {}));
    }

    /** Tests lazy {@link Provides#bindingMode()} on instance fields. */
    @Test
    public void bindLazyInstanceFields() {
        InstanceFields.test(c -> c.bindLazy(InstanceFields.class));
        InstanceFields.test(c -> c.bindLazy(Factory.findInjectable(InstanceFields.class)));
        InstanceFields.test(c -> c.bindLazy(new TypeLiteral<InstanceFields>() {}));
    }

    /** Tests lazy {@link Provides#bindingMode()} on instance fields. */
    @Test
    public void bindLazyInstanceFieldsLazyInitialization() {
        // Singleton
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(new AtomicBoolean(false));
            c.bindLazy(InstanceFieldSingleton.class);
        });
        assertThat(i.with(AtomicBoolean.class)).isTrue();
        InstanceFieldSingleton f = i.with(InstanceFieldSingleton.class);
        assertThat(i.with(Short.class)).isEqualTo((short) 1);
        f.S = 2;
        assertThat(i.with(Short.class)).isEqualTo((short) 1);

        // Lazy
        i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(new AtomicBoolean(false));
            c.bindLazy(InstanceFieldLazy.class);
        });
        assertThat(i.with(AtomicBoolean.class)).isFalse();
        assertThat(i.with(Short.class)).isEqualTo((short) 1);
        assertThat(i.with(AtomicBoolean.class)).isTrue();
        i.with(InstanceFieldLazy.class).S = 2;
        assertThat(i.with(Short.class)).isEqualTo((short) 1);
        // InstanceFieldsLazy.test(c -> c.bindLazy(Factory.findInjectable(InstanceFieldsLazy.class)));
        // InstanceFieldsLazy.test(c -> c.bindLazy(new TypeLiteral<InstanceFieldsLazy>() {}));
    }

    @Test
    @Disabled
    public void instanceFields() {

        InstanceFields.test(c -> c.bindPrototype(InstanceFields.class));
        InstanceFields.test(c -> c.bindPrototype(Factory.findInjectable(InstanceFields.class)));
        InstanceFields.test(c -> c.bindPrototype(new TypeLiteral<InstanceFields>() {}));
    }

    static class BindStaticInstantiable {

        @Provides(bindingMode = BindingMode.LAZY)
        private static Long L;

        @Provides(bindingMode = BindingMode.PROTOTYPE)
        private static Integer P;

        @Provides(bindingMode = BindingMode.SINGLETON)
        private static Short S;

        static void test(Consumer<? super InjectorConfiguration> configurator) {
            L = 1L;
            P = 1;
            S = 1;
            Injector i = Injector.of(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });
            assertThat(i.with(BindStaticInstantiable.class)).isNotNull();
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

    static class BindStaticNoInstantiation {

        @Provides(bindingMode = BindingMode.LAZY)
        private static Long L;

        @Provides(bindingMode = BindingMode.PROTOTYPE)
        private static Integer P;

        @Provides(bindingMode = BindingMode.SINGLETON)
        private static Short S;

        public BindStaticNoInstantiation() {
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

    static class InstanceFieldSingleton {

        @Provides(bindingMode = BindingMode.SINGLETON)
        Short S = 1;

        InstanceFieldSingleton(AtomicBoolean b) {
            b.set(true);
        }
    }

    static class InstanceFieldLazy {

        @Provides(bindingMode = BindingMode.LAZY)
        Short S = 1;

        InstanceFieldLazy(AtomicBoolean b) {
            b.set(true);
            new Exception().printStackTrace();
        }
    }

    static class InstanceFieldPrototype {

        @Provides(bindingMode = BindingMode.PROTOTYPE)
        Short S = 1;

        InstanceFieldPrototype(AtomicBoolean b) {
            b.set(true);
        }
    }
}
