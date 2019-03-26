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
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.TypeLiteral;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.Letters.C;
import support.stubs.Letters.D;
import support.stubs.Letters.E;
import support.stubs.Letters.F;
import support.stubs.Letters.G;
import support.stubs.Letters.H;
import support.stubs.Letters.I;
import support.stubs.Letters.J;

/**
 *
 */
@SuppressWarnings("unused")
public class ProvideTest {

    @Test
    public void configurationSite() throws Throwable {
        Injector inj = Injector.of(conf -> {
            conf.lookup(MethodHandles.lookup());// The module where letter classes are in are not exported
            ServiceConfiguration<A> a = conf.provide(A.class);
            ServiceConfiguration<B> b = conf.provide(Factory.findInjectable(B.class));
            ServiceConfiguration<C> c = conf.provide(C0);
            ServiceConfiguration<D> d = conf.provide(TypeLiteral.of(D.class));
            ServiceConfiguration<E> e = conf.provide(E.class).lazy();
            ServiceConfiguration<F> f = conf.provide(Factory.findInjectable(F.class)).lazy();
            ServiceConfiguration<G> g = conf.provide(TypeLiteral.of(G.class)).lazy();
            ServiceConfiguration<H> h = conf.provide(H.class).prototype();
            ServiceConfiguration<I> i = conf.provide(Factory.findInjectable(I.class)).prototype();
            ServiceConfiguration<J> j = conf.provide(TypeLiteral.of(J.class)).prototype();
        });
    }

    @Test
    public void bindInstance() {
        Injector i = Injector.of(e -> {
            ServiceConfiguration<A> sc = e.provide(A0);
            testConfiguration(sc, InstantiationMode.SINGLETON, Key.of(A.class));
        });
        testSingleton(i, Key.of(A.class), A0);

    }

    static <T> void testSingleton(Injector i, Key<T> key, T instance) {
        assertThat(i.get(key)).containsSame(instance);
        assertThat(i.with(key)).isSameAs(instance);
        if (!key.hasQualifier()) {
            @SuppressWarnings("unchecked")
            Class<T> rawType = (Class<T>) key.typeLiteral().getRawType();
            assertThat(i.get(rawType)).containsSame(instance);
            assertThat(i.with(rawType)).isSameAs(instance);
        }
    }

    static void testConfiguration(ServiceConfiguration<?> sc, InstantiationMode instantionMode, Key<?> key) {

        assertThat(sc.instantiationMode()).isSameAs(InstantiationMode.SINGLETON);
        // ConfigurationSite;
        assertThat(sc.getDescription()).isNull();
        assertThat(sc.getKey()).isEqualTo(Key.of(A.class));
        assertThat(sc.tags().isEmpty());
    }
}
