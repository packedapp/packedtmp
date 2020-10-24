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
package tck.injectorconfigurator.of.provide;

import static org.assertj.core.api.Assertions.assertThat;
import static testutil.stubs.Letters.A0;
import static testutil.stubs.Letters.C0;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.base.Key;
import app.packed.component.BeanConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.sandbox.PrototypeConfiguration;
import packed.internal.inject.service.sandbox.Injector;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.B;
import testutil.stubs.Letters.C;
import testutil.stubs.Letters.H;
import testutil.stubs.Letters.I;

/**
 *
 */
@SuppressWarnings("unused")
public class ProvideTest {

    @Test
    public void configSite() throws Throwable {
        Injector inj = Injector.configure(conf -> {
            conf.lookup(MethodHandles.lookup());// The module where letter classes are in are not exported
            BeanConfiguration<A> a = conf.provide(A.class);
            BeanConfiguration<B> b = conf.provide(Factory.of(B.class));
            BeanConfiguration<C> c = conf.provideInstance(C0);
            // ServiceComponentConfiguration<E> e = conf.provide(E.class).lazy();
            // ServiceComponentConfiguration<F> f = conf.provide(Factory.findInjectable(F.class)).lazy();
            PrototypeConfiguration<H> h = conf.providePrototype(H.class);
            PrototypeConfiguration<I> i = conf.providePrototype(Factory.of(I.class));
        });
    }

    @Test
    public void bindInstance() {
        Injector i = Injector.configure(e -> {
            BeanConfiguration<A> sc = e.provideInstance(A0);
            testConfiguration(sc, true, Key.of(A.class));
        });
        testSingleton(i, Key.of(A.class), A0);

    }

    static <T> void testSingleton(Injector i, Key<T> key, T instance) {
        assertThat(i.findInstance(key)).containsSame(instance);
        assertThat(i.use(key)).isSameAs(instance);
        if (!key.hasQualifier()) {
            @SuppressWarnings("unchecked")
            Class<T> rawType = (Class<T>) key.typeLiteral().rawType();
            assertThat(i.findInstance(rawType)).containsSame(instance);
            assertThat(i.use(rawType)).isSameAs(instance);
        }
    }

    static void testConfiguration(BeanConfiguration<?> sc, boolean isConstant, Key<?> key) {

        // assertThat(sc.instantiationMode()).isSameAs(ServiceMode.SINGLETON);
        // configSite;
        assertThat(sc.key().get()).isEqualTo(Key.of(A.class));
        // assertThat(sc.tags().isEmpty());
    }
}
