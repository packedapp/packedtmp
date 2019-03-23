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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;

/**
 *
 */
public class InjectorTagsTest {

    @Test
    public void tags() {
        assertThat(Injector.of(c -> {}).tags()).isEmpty();

        Injector i = Injector.of(c -> {
            assertThat(c.tags()).isEmpty();

            assertThat(c.tags().add("foo")).isTrue();
            assertThat(c.tags()).contains("foo");
            assertThat(c.tags().add("foo")).isFalse();
            assertThatNullPointerException().isThrownBy(() -> c.tags().add(null));
        });
        assertThat(i.tags()).containsExactly("foo");
    }
}
