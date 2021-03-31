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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import packed.internal.inject.service.sandbox.Injector;
import tck.injectorconfigurator.of.VarArgsInjectionTest.ArrayConstructor;
import tck.injectorconfigurator.of.VarArgsInjectionTest.VarArgsConstructor;

/** Tests that we can inject varargs as ordinary arrays. */
public class VarArgsInjectionTest {
    static String[] array = new String[] { "A", "B" };

    @Test
    // Fix this test
    public void varargs() {
        Injector i = Injector.configure(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(VarArgsConstructor.class);
            c.provideInstance(array);
        });
        assertThat(i.use(String[].class)).isSameAs(array);
    }

    @Test
    public void array() {
        Injector i = Injector.configure(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(ArrayConstructor.class);
            c.provideInstance(array);
        });
        assertThat(i.use(String[].class)).isSameAs(array);
    }

    public static class ArrayConstructor {
        final String[] strings;

        public ArrayConstructor(String[] strings) {
            this.strings = requireNonNull(strings);
        }
    }

    public static class VarArgsConstructor {
        final String[] strings;

        public VarArgsConstructor(String... strings) {
            this.strings = requireNonNull(strings);
        }
    }
}
