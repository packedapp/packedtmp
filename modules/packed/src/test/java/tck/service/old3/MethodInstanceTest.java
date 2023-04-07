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
package tck.service.old3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import app.packed.bean.BeanInstallationException;
import app.packed.operation.Op0;
import app.packed.service.Provide;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceLocator.Composer;

/** Tests {@link Provide#constant()}. */
public class MethodInstanceTest {

    /** Tests default {@link Provide#constant()} on instance methods. */
    @Test
    public void provide() {
        MixedMethods.test(c -> c.provideInstance(new MixedMethods()));
        MixedMethods.test(c -> c.provide(MixedMethods.class));
        MixedMethods.test(c -> c.provide(new Op0<>(MixedMethods::new) {}));
    }

    // /** Tests lazy {@link Provide2#instantionMode()} on instance methods. */
    // @Test
    // public void provideLazy() {
    // MixedMethods.test(c -> c.provide(MixedMethods.class).lazy());
    // MixedMethods.test(c -> c.provide(Factory.findInjectable(MixedMethods.class)).lazy());
    // MixedMethods.test(c -> c.provide(Factory.findInjectable(new TypeLiteral<MixedMethods>() {})).lazy());
    // // Correct support FOR LAZY->LAZY and LAZY->PROTOTYPE is not implemented yet.
    // // As we instantiate the parent no matter what. We just dont test it here
    // }

    /** Can never bind prototypes that have non-static provided fields. */
    @Test
    public void providePrototype() {
        AbstractThrowableAssert<?, ?> a = assertThatThrownBy(() -> ServiceLocator.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.provideInstance(new AtomicBoolean());
            c.providePrototype(SingletonMethod.class);
        }));
        a.isExactlyInstanceOf(BeanInstallationException.class).hasNoCause();
        // TODO check message

        // a = assertThatThrownBy(() -> Injector.configure(c -> {
        // c.lookup(MethodHandles.lookup());
        // c.provideInstance(new AtomicBoolean());
        // // c.provide(LazyMethod.class).prototype();
        // }));
        // a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO check message

        a = assertThatThrownBy(() -> ServiceLocator.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.provideInstance(new AtomicBoolean());
            c.providePrototype(PrototypeMethod.class);
        }));
        a.isExactlyInstanceOf(BeanInstallationException.class).hasNoCause();
        // TODO check message
    }

    // static class LazyMethod {
    //
    // Short s = 1;
    //
    // LazyMethod(AtomicBoolean b) {
    // b.set(true);
    // }
    //
    // @Provide2(instantionMode = InstantiationMode.LAZY)
    // public Short s() {
    // return s;
    // }
    // }

    public static class MixedMethods {

        // Long l = 1L;

        Integer p = 1;

        Short s = 1;

        // @Provide2(instantionMode = InstantiationMode.LAZY)
        // Long l() {
        // return l;
        // }

        @Provide
        Integer p() {
            return p;
        }

        @Provide
        Short s() {
            return s;
        }

        static void test(Consumer<? super Composer> configurator) {
            ServiceLocator i = ServiceLocator.of(c -> {
                c.lookup(MethodHandles.lookup());
                configurator.accept(c);
            });
            MixedMethods f = i.use(MixedMethods.class);
            // f.l = 2L;
            f.s = 2;
            f.p = 2;

            assertThat(i.use(Short.class)).isEqualTo((short) 2);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(2);
            // f.l = 3L;
            f.s = 3;
            f.p = 3;
            assertThat(i.use(Short.class)).isEqualTo((short) 3);
            // assertThat(i.use(Long.class)).isEqualTo(2L);
            assertThat(i.use(Integer.class)).isEqualTo(3);
        }
    }

    public static class PrototypeMethod {

        Short s = 1;

        public PrototypeMethod(AtomicBoolean b) {
            b.set(true);
        }

        @Provide
        public Short s() {
            return s;
        }
    }

    public static class SingletonMethod {

        Short s = 1;

        public SingletonMethod(AtomicBoolean b) {
            b.set(true);
        }

        @Provide
        public Short s() {
            return s;
        }
    }
}
