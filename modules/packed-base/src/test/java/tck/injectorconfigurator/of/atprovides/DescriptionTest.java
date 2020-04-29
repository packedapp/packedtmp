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
package tck.injectorconfigurator.of.atprovides;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.service.Injector;
import app.packed.service.InjectorAssembler;
import app.packed.service.Provide;

/** Tests {@link Provide#description()}. */
public class DescriptionTest {

    /** Tests service with description on {@link Provide}. */
    @Test
    @Disabled
    public void injectorWithDescription() {
        Injector i = of(c -> c.provideConstant(new WithDescription()));
        assertThat(i.getDescriptor(Long.class).get().description()).hasValue("niceField");
        assertThat(i.getDescriptor(Integer.class).get().description()).hasValue("niceMethod");
    }

    /** Tests service without description on {@link Provide}. */
    @Test
    public void injectorWithoutDescription() {
        Injector i = of(c -> c.provideConstant(new WithoutDescription()));
        assertThat(i.getDescriptor(Long.class).get().description()).isEmpty();
        assertThat(i.getDescriptor(Integer.class).get().description()).isEmpty();
    }

    private static Injector of(Consumer<? super InjectorAssembler> consumer) {
        return Injector.configure(c -> {
            c.lookup(MethodHandles.lookup());
            consumer.accept(c);
        });
    }

    static class WithDescription {

        @Provide(description = "niceField")
        public static final Long F = 0L;

        @Provide(description = "niceMethod")
        public static int m() {
            return 0;
        }
    }

    static class WithoutDescription {

        @Provide
        public static final Long F = 0L;

        @Provide
        public static int m() {
            return 0;
        }
    }
}
