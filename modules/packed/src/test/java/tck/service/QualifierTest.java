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

import org.junit.jupiter.api.Test;

import app.packed.service.Provide;
import app.packed.util.Key;
import tck.ServiceLocatorAppTest;
import testutil.stubs.Qualifiers.StringQualifier;

/**
 *
 */
public class QualifierTest extends ServiceLocatorAppTest {

    @Test
    public void multipleFields() {
        // Stub.A = 1L;
        Stub.B = 1L;
        Stub.C = 1L;
        Stub.L = 1L;

        base().exportAll();
        install(Stub.class);
        assertThat(app().use(new Key<@StringQualifier("B") Long>() {})).isEqualTo(1L);
        assertThat(app().use(new Key<@StringQualifier("C") Long>() {})).isEqualTo(1L);
        assertThat(app().use(new Key<Long>() {})).isEqualTo(1L);
        // Stub.A = 2L;
        Stub.B = 2L;
        Stub.C = 2L;
        Stub.L = 2L;

        // assertThat(i.use(new Key<@StringQualifier("A") Long>() {})).isEqualTo(2L);
        assertThat(app().use(new Key<@StringQualifier("B") Long>() {})).isEqualTo(2L);
        assertThat(app().use(new Key<@StringQualifier("C") Long>() {})).isEqualTo(2L);
        assertThat(app().use(new Key<Long>() {})).isEqualTo(2L);

        // Stub.A = 3L;
        Stub.B = 3L;
        Stub.C = 3L;
        Stub.L = 3L;

        // assertThat(i.use(new Key<@StringQualifier("A") Long>() {})).isEqualTo(2L);
        assertThat(app().use(new Key<@StringQualifier("B") Long>() {})).isEqualTo(3L);
        assertThat(app().use(new Key<@StringQualifier("C") Long>() {})).isEqualTo(3L);
        assertThat(app().use(new Key<Long>() {})).isEqualTo(3L);
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
