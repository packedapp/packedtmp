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
package testutil.stubs;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.util.TestMemberFinder.findField;

import java.lang.reflect.Field;

import app.packed.inject.Inject;
import testutil.stubs.Letters.A;

/**
 *
 */
public class Fields {

    @SuppressWarnings("unused")
    private static String FINAL_FIELD$ = "ff";

    public static Field FINAL_FIELD = findField("FINAL_FIELD$");

    public static class InjectAConstructor {

        public final A a;

        public InjectAConstructor(A a) {
            this.a = requireNonNull(a);
        }
    }

    public static class InjectAField {
        @Inject
        public A a;
    }

    public static class FinalAField {
        @Inject
        public final A value = Letters.A0;
    }

    public static class InjectAMethod {
        public A value;

        @Inject
        public void injectMe(A value) {
            this.value = value;
        }
    }

    public static class PrimitivesLong {
        public final long expected;

        @Inject
        private Long field;

        @Inject
        private long fieldPrimitive;

        private boolean methodInjected;

        private boolean methodPrimitiveInjected;

        public PrimitivesLong(long expected) {
            this.expected = expected;
        }

        @Inject
        public void inject(long value) {
            assertEquals(expected, value);
            methodInjected = true;
        }

        @Inject
        public void injectPrimitive(long value) {
            assertEquals(expected, value);
            methodPrimitiveInjected = true;
        }

        public void verify() {
            assertEquals(Long.valueOf(expected), this.field, "field was not injected");
            assertEquals(expected, this.fieldPrimitive, "private field was not injected");

            assertTrue(methodInjected, "method was not injected");
            assertTrue(methodPrimitiveInjected, "method with primitive parameter was not injected");
        }
    }

}
