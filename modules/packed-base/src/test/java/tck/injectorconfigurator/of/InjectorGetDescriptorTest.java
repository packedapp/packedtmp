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

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.base.Key;
import app.packed.inject.Service;
import app.packed.inject.sandbox.Injector;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.B;
import testutil.stubs.annotation.Left;
import testutil.stubs.annotation.Right;

/** Test {@link Injector#findService(Class)} and {@link Injector#findService(Key)}. */
public class InjectorGetDescriptorTest {

    @Test
    public void get() {
        Injector i = Injector.configure(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(A.class);
            c.provide(A.class).as(new Key<@Left A>() {});
        });

        Service a = i.findService(A.class).get();

        // TODO configSite
        assertThat(a.key()).isEqualTo(Key.of(A.class));
        // assertThat(a.tags()).isEmpty();

        Service aLeft = i.findService(new Key<@Left A>() {}).get();
        // TODO configSite
        assertThat(aLeft.key()).isEqualTo(new Key<@Left A>() {});
        // assertThat(aLeft.tags()).isEmpty();

        assertThat(i.findService(B.class)).isEmpty();
        assertThat(i.findService(new Key<@Left B>() {})).isEmpty();
        assertThat(i.findService(new Key<@Right A>() {})).isEmpty();

    }
}
