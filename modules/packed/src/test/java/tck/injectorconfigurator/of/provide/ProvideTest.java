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

import app.packed.bean.BeanConfiguration;
import app.packed.bindings.Key;
import app.packed.operation.Op0;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.service.ServiceLocator;
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
        ServiceLocator inj = ServiceLocator.of(conf -> {
            conf.lookup(MethodHandles.lookup());// The module where letter classes are in are not exported
            ServiceableBeanConfiguration<A> a = conf.provide(A.class);
            ServiceableBeanConfiguration<B> b = conf.provide(new Op0<>(B::new) {});
            ServiceableBeanConfiguration<C> c = conf.provideInstance(C0);
            // ServiceComponentConfiguration<E> e = conf.provide(E.class).lazy();
            // ServiceComponentConfiguration<F> f = conf.provide(Factory.findInjectable(F.class)).lazy();
            BeanConfiguration h = conf.providePrototype(H.class);
            BeanConfiguration i = conf.providePrototype(new Op0<>(I::new) {});
        });
    }

    @Test
    public void bindInstance() {
        ServiceLocator i = ServiceLocator.of(e -> {
            ServiceableBeanConfiguration<A> sc = e.provideInstance(A0);
            testConfiguration(sc, true, Key.of(A.class));
        });
        testSingleton(i, Key.of(A.class), A0);

    }

    static <T> void testSingleton(ServiceLocator i, Key<T> key, T instance) {
        assertThat(i.findInstance(key)).containsSame(instance);
        assertThat(i.use(key)).isSameAs(instance);
        if (!key.isQualified()) {
            @SuppressWarnings("unchecked")
            Class<T> rawType = (Class<T>) key.rawType();
            assertThat(i.findInstance(rawType)).containsSame(instance);
            assertThat(i.use(rawType)).isSameAs(instance);
        }
    }

    static void testConfiguration(ServiceableBeanConfiguration<?> sc, boolean isConstant, Key<?> key) {

        // assertThat(sc.instantiationMode()).isSameAs(ServiceMode.SINGLETON);
        // configSite;
     //   assertThat(sc.providedAs().get()).isEqualTo(Key.of(A.class));
        // assertThat(sc.tags().isEmpty());
    }
}
