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
import static support.stubs.Letters.A0;
import static support.stubs.Letters.C0;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ComponentServiceConfiguration;
import app.packed.util.Key;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.Letters.C;
import support.stubs.Letters.E;
import support.stubs.Letters.F;
import support.stubs.Letters.H;
import support.stubs.Letters.I;

/**
 *
 */
@SuppressWarnings("unused")
public class ProvideTest {

    @Test
    public void configSite() throws Throwable {
        Injector inj = Injector.configure(conf -> {
            conf.lookup(MethodHandles.lookup());// The module where letter classes are in are not exported
            ComponentServiceConfiguration<A> a = conf.provide(A.class);
            ComponentServiceConfiguration<B> b = conf.provide(Factory.findInjectable(B.class));
            ComponentServiceConfiguration<C> c = conf.provideConstant(C0);
            ComponentServiceConfiguration<E> e = conf.provide(E.class).lazy();
            ComponentServiceConfiguration<F> f = conf.provide(Factory.findInjectable(F.class)).lazy();
            ComponentServiceConfiguration<H> h = conf.provide(H.class).prototype();
            ComponentServiceConfiguration<I> i = conf.provide(Factory.findInjectable(I.class)).prototype();
        });
    }

    @Test
    public void bindInstance() {
        Injector i = Injector.configure(e -> {
            ComponentServiceConfiguration<A> sc = e.provideConstant(A0);
            testConfiguration(sc, InstantiationMode.SINGLETON, Key.of(A.class));
        });
        testSingleton(i, Key.of(A.class), A0);

    }

    static <T> void testSingleton(Injector i, Key<T> key, T instance) {
        assertThat(i.get(key)).containsSame(instance);
        assertThat(i.use(key)).isSameAs(instance);
        if (!key.hasQualifier()) {
            @SuppressWarnings("unchecked")
            Class<T> rawType = (Class<T>) key.typeLiteral().rawType();
            assertThat(i.get(rawType)).containsSame(instance);
            assertThat(i.use(rawType)).isSameAs(instance);
        }
    }

    static void testConfiguration(ComponentServiceConfiguration<?> sc, InstantiationMode instantionMode, Key<?> key) {

        assertThat(sc.instantiationMode()).isSameAs(InstantiationMode.SINGLETON);
        // configSite;
        assertThat(sc.getDescription()).isNull();
        assertThat(sc.getKey()).isEqualTo(Key.of(A.class));
        // assertThat(sc.tags().isEmpty());
    }
}
