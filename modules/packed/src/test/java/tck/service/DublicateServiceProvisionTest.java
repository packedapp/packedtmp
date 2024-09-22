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
package tck.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import app.packed.binding.Key;
import app.packed.binding.KeyAlreadyInUseException;
import app.packed.service.Provide;
import tck.AppAppTest;
import testutil.stubs.Qualifiers.IntQualifier;
import testutil.stubs.Qualifiers.StringQualifier;

/**
 *
 */
public class DublicateServiceProvisionTest extends AppAppTest {

    @Test
    public void dublicateKeyDifferentBeans() {
        installInstance(1L).provideAs(Number.class);
        Assertions.assertThrows(KeyAlreadyInUseException.class, () -> installInstance(1).provideAs(Number.class));
        // TODO check message

        reset();
        installInstance(1L).provideAs(new Key<Number>() {});
        Assertions.assertThrows(KeyAlreadyInUseException.class, () -> installInstance(1).provideAs(Number.class));

        reset();
        installInstance(1L).provideAs(new Key<@IntQualifier Number>() {});
        Assertions.assertThrows(KeyAlreadyInUseException.class, () -> installInstance(1).provideAs(new Key<@IntQualifier Number>() {}));

    }

    @Test
    public void cannotDefineSameProvidedKeys() {
        KeyAlreadyInUseException e = assertThrows(KeyAlreadyInUseException.class, () -> install(MultipleIdenticalQualifiedFieldKeys.class));
        assertThat(e).hasNoCause();
        reset();

        e = assertThrows(KeyAlreadyInUseException.class, () -> install(MultipleIdenticalQualifiedMethodKeys.class));
        assertThat(e).hasNoCause();
        reset();

        e = assertThrows(KeyAlreadyInUseException.class, () -> install(MultipleIdenticalQualifiedMemberKeys.class));
        assertThat(e).hasNoCause();
        reset();
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
}
