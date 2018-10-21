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
package packed.util;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import packed.util.ThrowableUtil;
import support.stubs.Throwables.Error1;
import support.stubs.Throwables.Exception1;
import support.stubs.Throwables.RuntimeException1;

/** Tests {@link ThrowableUtil}. */
public class ThrowableUtilTest {

    @Test
    public void rethrowErrorOrRuntimeException() {
        try {
            ThrowableUtil.rethrowErrorOrRuntimeException(Error1.INSTANCE);
            fail("Oops");
        } catch (Error ok) {
            assertSame(Error1.INSTANCE, ok);
        }

        try {
            ThrowableUtil.rethrowErrorOrRuntimeException(RuntimeException1.INSTANCE);
            fail("Oops");
        } catch (RuntimeException ok) {
            assertSame(RuntimeException1.INSTANCE, ok);
        }

        assertSame(Exception1.INSTANCE, ThrowableUtil.rethrowErrorOrRuntimeException(Exception1.INSTANCE));
    }
}
