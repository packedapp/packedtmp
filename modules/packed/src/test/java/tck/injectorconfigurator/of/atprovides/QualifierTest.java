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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import app.packed.service.Provide;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceLocator.Composer;
import app.packed.util.Key;
import app.packed.util.KeyAlreadyInUseException;
import testutil.stubs.annotation.StringQualifier;

/**
 *
 */
public class QualifierTest {

    @Test
    public void cannotDefineSameProvidedKeys() {
        AbstractThrowableAssert<?, ?> at = assertThatThrownBy(() -> create(c -> c.provide(MultipleIdenticalQualifiedFieldKeys.class)));
        at.isExactlyInstanceOf(KeyAlreadyInUseException.class);
        at.hasNoCause();
        // TODO check message

        at = assertThatThrownBy(() -> create(c -> c.provide(MultipleIdenticalQualifiedMethodKeys.class)));
        at.isExactlyInstanceOf(KeyAlreadyInUseException.class);
        at.hasNoCause();

        at = assertThatThrownBy(() -> create(c -> c.provide(MultipleIdenticalQualifiedMemberKeys.class)));
        at.isExactlyInstanceOf(KeyAlreadyInUseException.class);
        at.hasNoCause();
    }

    @Test
    public void multipleFields() {
        // Stub.A = 1L;
        Stub.B = 1L;
        Stub.C = 1L;
        Stub.L = 1L;
        ServiceLocator i = create(c -> {
            c.provide(Stub.class);

        });
        // Stub.A = 2L;
        Stub.B = 2L;
        Stub.C = 2L;
        Stub.L = 2L;

        // assertThat(i.use(new Key<@StringQualifier("A") Long>() {})).isEqualTo(2L);
        assertThat(i.use(new Key<@StringQualifier("B") Long>() {})).isEqualTo(2L);
        assertThat(i.use(new Key<@StringQualifier("C") Long>() {})).isEqualTo(2L);
        assertThat(i.use(new Key<Long>() {})).isEqualTo(2L);

        // Stub.A = 3L;
        Stub.B = 3L;
        Stub.C = 3L;
        Stub.L = 3L;

        // assertThat(i.use(new Key<@StringQualifier("A") Long>() {})).isEqualTo(2L);
        assertThat(i.use(new Key<@StringQualifier("B") Long>() {})).isEqualTo(3L);
        assertThat(i.use(new Key<@StringQualifier("C") Long>() {})).isEqualTo(3L);
        assertThat(i.use(new Key<Long>() {})).isEqualTo(3L);
    }

    private static ServiceLocator create(Consumer<? super Composer> consumer) {
        return ServiceLocator.of(c -> {
            c.lookup(MethodHandles.lookup());
            consumer.accept(c);
        });
    }

    public static class MultipleIdenticalQualifiedFieldKeys {

        @Provide
        @StringQualifier("A")
        private Long A = 0L;

        @Provide
        @StringQualifier("A")
        private Long B = 0L;
    }

    public static class MultipleIdenticalQualifiedMemberKeys {

        @Provide
        @StringQualifier("A")
        private Long A = 0L;

        @Provide
        @StringQualifier("A")
        static Long b() {
            return 0L;
        }
    }

    public static class MultipleIdenticalQualifiedMethodKeys {

        @Provide
        @StringQualifier("A")
        static Long a() {
            return 0L;
        }

        @Provide
        @StringQualifier("A")
        static Long b() {
            return 0L;
        }
    }

    public static class Stub {

        // @Provide(instantionMode = InstantiationMode.LAZY)
        // @StringQualifier("A")
        // private static Long A;

        @Provide
        @StringQualifier("B")
        private static Long B;

        @Provide
        @StringQualifier("C")
        private static Long C;

        @Provide
        private static Long L;
    }
}
