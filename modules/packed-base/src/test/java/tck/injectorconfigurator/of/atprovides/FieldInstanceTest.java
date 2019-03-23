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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.InstantiationMode;
import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.TypeLiteral;

/** Tests {@link Provides#instantionMode()} on fields. */
public class FieldInstanceTest {

    /** Tests default {@link Provides#instantionMode()} on instance fields. */
    @Test
    public void provide() {
        MixedFields.test(c -> c.provide(new MixedFields()));
        MixedFields.test(c -> c.provide(MixedFields.class));
        MixedFields.test(c -> c.provide(Factory.findInjectable(MixedFields.class)));
        MixedFields.test(c -> c.provide(new TypeLiteral<MixedFields>() {}));
    }

    /** Tests lazy {@link Provides#instantionMode()} on instance fields. */
    @Test
    public void provideLazy() {
        MixedFields.test(c -> c.provideLazy(MixedFields.class));
        MixedFields.test(c -> c.provideLazy(Factory.findInjectable(MixedFields.class)));
        MixedFields.test(c -> c.provideLazy(new TypeLiteral<MixedFields>() {}));
    }

    /**
     * An extra test for lazy {@link Provides#instantionMode()} on instance fields. Which makes sure that the lazy parent is
     * not created before it is needed by the provided fields.
     */
    @Test
    @Disabled
    public void provideLazy2() {
        // Singleton
        Injector i = of(c -> {
            c.provide(new AtomicBoolean(false));
            c.provideLazy(SingletonField.class);
        });
        assertThat(i.with(AtomicBoolean.class)).isTrue();
        SingletonField f = i.with(SingletonField.class);
        assertThat(i.with(Short.class)).isEqualTo((short) 1);
        f.s = 2;
        assertThat(i.with(Short.class)).isEqualTo((short) 1);

        // Lazy
        i = of(c -> {
            c.provide(new AtomicBoolean(false));
            c.provideLazy(LazyField.class);
        });
        assertThat(i.with(AtomicBoolean.class)).isFalse();
        assertThat(i.with(Short.class)).isEqualTo((short) 1);
        assertThat(i.with(AtomicBoolean.class)).isTrue();
        i.with(LazyField.class).s = 2;
        assertThat(i.with(Short.class)).isEqualTo((short) 1);
        // InstanceFieldsLazy.test(c -> c.bindLazy(Factory.findInjectable(InstanceFieldsLazy.class)));
        // InstanceFieldsLazy.test(c -> c.bindLazy(new TypeLiteral<InstanceFieldsLazy>() {}));

        // Correct support FOR LAZY->LAZY and LAZY->PROTOTYPE is not implemented yet.
        // As we instantiate the parent no matter what
    }

    /** Can never bind prototypes that have non-static provided fields. */
    @Test
    public void providePrototype() {
        AbstractThrowableAssert<?, ?> a = assertThatThrownBy(() -> of(c -> {
            c.provide(new AtomicBoolean());
            c.providePrototype(SingletonField.class);
        }));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO check message

        a = assertThatThrownBy(() -> of(c -> {
            c.provide(new AtomicBoolean());
            c.providePrototype(LazyField.class);
        }));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO check message

        a = assertThatThrownBy(() -> Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(new AtomicBoolean());
            c.providePrototype(PrototypeField.class);
        }));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO check message
    }

    private static Injector of(Consumer<? super InjectorConfigurator> consumer) {
        return Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            consumer.accept(c);
        });
    }

    static class LazyField {

        @Provides(instantionMode = InstantiationMode.LAZY)
        Short s = 1;

        LazyField(AtomicBoolean b) {
            b.set(true);
        }
    }

    static class MixedFields {

        @Provides(instantionMode = InstantiationMode.LAZY)
        Long l = 1L;

        @Provides(instantionMode = InstantiationMode.PROTOTYPE)
        Integer p = 1;

        @Provides(instantionMode = InstantiationMode.SINGLETON)
        Short s = 1;

        static void test(Consumer<? super InjectorConfigurator> configurator) {
            Injector i = of(c -> configurator.accept(c));
            MixedFields f = i.with(MixedFields.class);
            f.l = 2L;
            f.s = 2;
            f.p = 2;

            assertThat(i.with(Short.class)).isEqualTo((short) 1);
            assertThat(i.with(Long.class)).isEqualTo(2L);
            assertThat(i.with(Integer.class)).isEqualTo(2);
            f.l = 3L;
            f.s = 3;
            f.p = 3;
            assertThat(i.with(Short.class)).isEqualTo((short) 1);
            assertThat(i.with(Long.class)).isEqualTo(2L);
            assertThat(i.with(Integer.class)).isEqualTo(3);
        }
    }

    static class PrototypeField {

        @Provides(instantionMode = InstantiationMode.PROTOTYPE)
        Short s = 1;

        PrototypeField(AtomicBoolean b) {
            b.set(true);
        }
    }

    static class SingletonField {

        @Provides(instantionMode = InstantiationMode.SINGLETON)
        Short s = 1;

        SingletonField(AtomicBoolean b) {
            b.set(true);
        }
    }
}
