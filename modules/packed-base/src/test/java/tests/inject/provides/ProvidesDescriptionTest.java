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

import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.inject.Provides;
import tests.inject.AbstractInjectorTest;

/** Tests {@link Provides#description()}. */
public class ProvidesDescriptionTest extends AbstractInjectorTest {

    /** Tests components with description on {@link Provides}. */
    @Test
    public void containerWithDescription() {
        Container i = Container.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.install(new WithDescription());
        });
        assertThat(i.getService(Long.class).description()).isEqualTo("niceField");
        assertThat(i.getService(Integer.class).description()).isEqualTo("niceMethod");
    }

    /** Tests service with description on {@link Provides}. */
    @Test
    public void containerWithoutDescription() {
        Container i = Container.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.install(new WithoutDescription());
        });
        assertThat(i.getService(Long.class).description()).isNull();
        assertThat(i.getService(Integer.class).description()).isNull();
    }

    /** Tests service with description on {@link Provides}. */
    @Test
    public void injectorWithDescription() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(new WithDescription());
        });
        assertThat(i.getService(Long.class).description()).isEqualTo("niceField");
        assertThat(i.getService(Integer.class).description()).isEqualTo("niceMethod");
    }

    /** Tests service without description on {@link Provides}. */
    @Test
    public void injectorWithoutDescription() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(new WithoutDescription());
        });
        assertThat(i.getService(Long.class).description()).isNull();
        assertThat(i.getService(Integer.class).description()).isNull();
    }

    static class WithDescription {

        @Provides(description = "niceField")
        public static final Long F = 0L;

        @Provides(description = "niceMethod")
        public static int m() {
            return 0;
        }
    }

    static class WithoutDescription {

        @Provides
        public static final Long F = 0L;

        @Provides
        public static int m() {
            return 0;
        }
    }
}
