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
package tck.injectorconfigurator.of;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandles;
import java.util.NoSuchElementException;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import app.packed.framework.Key;
import app.packed.service.ServiceLocator;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.AExt;
import testutil.stubs.Letters.B;
import testutil.stubs.annotation.Left;
import testutil.stubs.annotation.Right;

/** Test {@link ServiceLocator#findInstance(Class)} and {@link ServiceLocator#findInstance(Key)}. */
public class InjectorWithTest {

    @Test
    public void get() {
        ServiceLocator i = ServiceLocator.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(A.class);
            c.install(AExt.class).provideAs(new Key<@Left A>() {});
        });

        assertThat(i.use(A.class)).isInstanceOf(A.class);
        assertThat(i.use(new Key<A>() {})).isInstanceOf(A.class);
        assertThat(i.use(new Key<A>() {})).isSameAs(i.findInstance(A.class).get());

        assertThat(i.use(new Key<@Left A>() {})).isInstanceOf(A.class);

        assertThat(i.use(new Key<A>() {})).isNotSameAs(i.use(new Key<@Left A>() {}));

        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> i.use(B.class));
        a.isExactlyInstanceOf(NoSuchElementException.class).hasNoCause();

        a = assertThatThrownBy(() -> i.use(new Key<@Left B>() {}));
        a.isExactlyInstanceOf(NoSuchElementException.class).hasNoCause();

        a = assertThatThrownBy(() -> i.use(new Key<@Right A>() {}));
        a.isExactlyInstanceOf(NoSuchElementException.class).hasNoCause();
    }
}
