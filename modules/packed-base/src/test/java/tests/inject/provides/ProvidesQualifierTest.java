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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandles;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import support.stubs.annotation.StringQualifier;

/**
 *
 */
public class ProvidesQualifierTest {

    @Test
    public void multipleFields() {
        Stub.A = 1L;
        Stub.B = 1L;
        Stub.C = 1L;
        Stub.L = 1L;
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(Stub.class);
        });
        Stub.A = 2L;
        Stub.B = 2L;
        Stub.C = 2L;
        Stub.L = 2L;

        assertThat(i.with(new Key<@StringQualifier("A") Long>() {})).isEqualTo(2L);
        assertThat(i.with(new Key<@StringQualifier("B") Long>() {})).isEqualTo(2L);
        assertThat(i.with(new Key<@StringQualifier("C") Long>() {})).isEqualTo(1L);
        assertThat(i.with(new Key<Long>() {})).isEqualTo(1L);

        Stub.A = 3L;
        Stub.B = 3L;
        Stub.C = 3L;
        Stub.L = 3L;

        assertThat(i.with(new Key<@StringQualifier("A") Long>() {})).isEqualTo(2L);
        assertThat(i.with(new Key<@StringQualifier("B") Long>() {})).isEqualTo(3L);
        assertThat(i.with(new Key<@StringQualifier("C") Long>() {})).isEqualTo(1L);
        assertThat(i.with(new Key<Long>() {})).isEqualTo(1L);
    }

    @Test
    public void cannotDefineSameProvidedKeys() {

        AbstractThrowableAssert<?, ?> at = assertThatThrownBy(() -> Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(MultipleIdenticalQualifiedFieldKeys.class);
        }));
        at.isExactlyInstanceOf(InvalidDeclarationException.class);
        at.hasNoCause();
        // TODO check message

        at = assertThatThrownBy(() -> Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(MultipleIdenticalQualifiedMethodKeys.class);
        }));
        at.isExactlyInstanceOf(InvalidDeclarationException.class);
        at.hasNoCause();

        at = assertThatThrownBy(() -> Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.bind(MultipleIdenticalQualifiedMemberKeys.class);
        }));
        at.isExactlyInstanceOf(InvalidDeclarationException.class);
        at.hasNoCause();

    }

    static class MultipleIdenticalQualifiedFieldKeys {

        @Provides
        @StringQualifier("A")
        private Long A = 0L;

        @Provides
        @StringQualifier("A")
        private Long B = 0L;
    }

    static class MultipleIdenticalQualifiedMethodKeys {

        @Provides
        @StringQualifier("A")
        static Long a() {
            return 0L;
        }

        @Provides
        @StringQualifier("A")
        static Long b() {
            return 0L;
        }
    }

    static class MultipleIdenticalQualifiedMemberKeys {

        @Provides
        @StringQualifier("A")
        private Long A = 0L;

        @Provides
        @StringQualifier("A")
        static Long b() {
            return 0L;
        }
    }

    static class Stub {

        @Provides(instantionMode = InstantiationMode.LAZY)
        @StringQualifier("A")
        private static Long A;

        @Provides(instantionMode = InstantiationMode.PROTOTYPE)
        @StringQualifier("B")
        private static Long B;

        @Provides(instantionMode = InstantiationMode.SINGLETON)
        @StringQualifier("C")
        private static Long C;

        @Provides
        private static Long L;
    }
}
