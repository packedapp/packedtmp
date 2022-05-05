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
import app.packed.inject.service.OldServiceLocator;
import app.packed.inject.service.Service;
import packed.internal.inject.service.sandbox.Injector;
import packed.internal.inject.service.sandbox.InjectorComposer;
import testutil.stubs.Letters.A;
import testutil.stubs.Letters.B;
import testutil.stubs.annotation.Left;
import testutil.stubs.annotation.Right;

/** Test {@link Injector#find(Class)} and {@link Injector#find(Key)}. */
public class InjectorGetDescriptorTest {

    @Test
    public void get() {
        OldServiceLocator i = InjectorComposer.configure(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(A.class);
            c.provide(A.class).provideAs(new Key<@Left A>() {});
        });

        Service a = i.find(A.class).get();

        // TODO configSite
        assertThat(a.key()).isEqualTo(Key.of(A.class));
        // assertThat(a.tags()).isEmpty();

        Service aLeft = i.find(new Key<@Left A>() {}).get();
        // TODO configSite
        assertThat(aLeft.key()).isEqualTo(new Key<@Left A>() {});
        // assertThat(aLeft.tags()).isEmpty();

        assertThat(i.find(B.class)).isEmpty();
        assertThat(i.find(new Key<@Left B>() {})).isEmpty();
        assertThat(i.find(new Key<@Right A>() {})).isEmpty();

    }
}
