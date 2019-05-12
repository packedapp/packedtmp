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

import app.packed.inject.Injector;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.annotation.Left;
import support.stubs.annotation.Right;

/** Test {@link Injector#getDescriptor(Class)} and {@link Injector#getDescriptor(Key)}. */
public class InjectorGetDescriptorTest {

    @Test
    public void get() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(A.class);
            c.provide(A.class).as(new Key<@Left A>() {});
        });

        ServiceDescriptor a = i.getDescriptor(A.class).get();

        // TODO configurationSite
        assertThat(a.description()).isEmpty();
        assertThat(a.key()).isEqualTo(Key.of(A.class));
        // assertThat(a.tags()).isEmpty();

        ServiceDescriptor aLeft = i.getDescriptor(new Key<@Left A>() {}).get();
        // TODO configurationSite
        assertThat(aLeft.description()).isEmpty();
        assertThat(aLeft.key()).isEqualTo(new Key<@Left A>() {});
        // assertThat(aLeft.tags()).isEmpty();

        assertThat(i.getDescriptor(B.class)).isEmpty();
        assertThat(i.getDescriptor(new Key<@Left B>() {})).isEmpty();
        assertThat(i.getDescriptor(new Key<@Right A>() {})).isEmpty();

    }
}
