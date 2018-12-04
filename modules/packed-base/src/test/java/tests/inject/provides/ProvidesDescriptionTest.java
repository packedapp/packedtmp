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
package tests.inject.provides;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;
import app.packed.inject.Provides;
import tests.inject.AbstractInjectorTest;

/** Tests {@link Provides#description()}. */
public class ProvidesDescriptionTest extends AbstractInjectorTest {

    @Test
    public void withDescription() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(new WithDescription());
        });
        assertThat(i.getService(Long.class).getDescription()).isEqualTo("niceField");
        assertThat(i.getService(Integer.class).getDescription()).isEqualTo("niceMethod");
    }

    @Test
    public void withoutDescription() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(new WithoutDescription());
        });
        assertThat(i.getService(Long.class).getDescription()).isNull();
        assertThat(i.getService(Integer.class).getDescription()).isNull();
    }

    static class WithoutDescription {

        @Provides
        public static final Long F = 0L;

        @Provides
        public static int m() {
            return 0;
        }
    }

    static class WithDescription {

        @Provides(description = "niceField")
        public static final Long F = 0L;

        @Provides(description = "niceMethod")
        public static int m() {
            return 0;
        }
    }
}
