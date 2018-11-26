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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;
import packed.internal.inject.runtimenodes.RuntimeServiceNode;
import support.stubs.Letters.A;

/**
 * Tests various things that do not have their own test class.
 */
public class InjectorTest {

    static final Injector EMPTY = Injector.of(c -> {});

    @Test
    public void description() {
        assertThat(EMPTY.getDescription()).isNull();

        Injector i = Injector.of(c -> {
            assertThat(c.getDescription()).isNull();

            assertThat(c.setDescription("fooo")).isSameAs(c);
            assertThat(c.getDescription()).isEqualTo("fooo");

            assertThat(c.setDescription(null)).isSameAs(c);
            assertThat(c.getDescription()).isNull();

            c.setDescription("final_desc");
        });
        assertThat(i.getDescription()).isEqualTo("final_desc");
    }

    @Test
    public void services() {
        assertThat(EMPTY.services()).isEmpty();
    }

    @Test
    public void tags() {
        assertThat(EMPTY.tags()).isEmpty();

        Injector i = Injector.of(c -> {
            assertThat(c.tags()).isEmpty();

            assertThat(c.tags().add("foo")).isTrue();
            assertThat(c.tags()).contains("foo");
            assertThat(c.tags().add("foo")).isFalse();
            assertThatNullPointerException().isThrownBy(() -> c.tags().add(null));
        });
        assertThat(i.tags()).containsExactly("foo");
    }

    @Test
    public void isRuntimeServices() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(A.class);
        });

        assertThat(i.getService(A.class)).isInstanceOf(RuntimeServiceNode.class);
    }
}
