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
package tests.inject;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;

/** Tests that we can inject varargs as ordinary arrays. */
public class VarArgsInjectionTest {
    static String[] array = new String[] { "A", "B" };

    public static void maind(String[] args) {
        for (Constructor<?> m : VarArgsConstructor.class.getDeclaredConstructors()) {
            System.out.println(m);
            System.out.println((m.isVarArgs()));
        }
    }

    public static void foo(String... args) {

    }

    @Test
    @Disabled
    // Fix this test
    public void varargs() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(VarArgsConstructor.class);
            c.bind(array);
        });
        assertThat(i.with(String[].class)).isSameAs(array);
    }

    @Test
    public void array() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(ArrayConstructor.class);
            c.bind(array);
        });
        assertThat(i.with(String[].class)).isSameAs(array);
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
