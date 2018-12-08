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
package tests.injector;

import static org.assertj.core.api.Assertions.assertThat;
import static support.stubs.Letters.A0;
import static support.stubs.Letters.C0;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.inject.InstantiationMode;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
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
public class InjectorConfiguationBindTest {

    @Test
    public void configurationSite() throws Throwable {
        Injector inj = Injector.of(conf -> {
            conf.lookup(MethodHandles.lookup());// The module where letter classes are in are not exported
            ServiceConfiguration<A> a = conf.bind(A.class);
            ServiceConfiguration<B> b = conf.bind(Factory.findInjectable(B.class));
            ServiceConfiguration<C> c = conf.bind(C0);
            ServiceConfiguration<D> d = conf.bind(TypeLiteral.of(D.class));
            ServiceConfiguration<E> e = conf.bindLazy(E.class);
            ServiceConfiguration<F> f = conf.bindLazy(Factory.findInjectable(F.class));
            ServiceConfiguration<G> g = conf.bindLazy(TypeLiteral.of(G.class));
            ServiceConfiguration<H> h = conf.bindPrototype(H.class);
            ServiceConfiguration<I> i = conf.bindPrototype(Factory.findInjectable(I.class));
            ServiceConfiguration<J> j = conf.bindPrototype(TypeLiteral.of(J.class));
        });
    }

    @Test
    public void bindInstance() {
        Injector i = Injector.of(e -> {
            ServiceConfiguration<A> sc = e.bind(A0);
            testConfiguration(sc, InstantiationMode.SINGLETON, Key.of(A.class));
        });
        testSingleton(i, Key.of(A.class), A0);

    }

    static <T> void testSingleton(Injector i, Key<T> key, T instance) {
        assertThat(i.get(key)).containsSame(instance);
        assertThat(i.with(key)).isSameAs(instance);
        if (!key.hasQualifier()) {
            @SuppressWarnings("unchecked")
            Class<T> rawType = (Class<T>) key.getTypeLiteral().getRawType();
            assertThat(i.get(rawType)).containsSame(instance);
            assertThat(i.with(rawType)).isSameAs(instance);
        }
    }

    static void testConfiguration(ServiceConfiguration<?> sc, InstantiationMode instantionMode, Key<?> key) {

        assertThat(sc.getInstantiationMode()).isSameAs(InstantiationMode.SINGLETON);
        // ConfigurationSite;
        assertThat(sc.getDescription()).isNull();
        assertThat(sc.getKey()).isEqualTo(Key.of(A.class));
        assertThat(sc.tags().isEmpty());
    }
}
