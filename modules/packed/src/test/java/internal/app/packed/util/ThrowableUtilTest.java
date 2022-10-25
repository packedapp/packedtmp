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
package internal.app.packed.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import testutil.stubs.Throwables.Exception1;

/** Tests {@link ThrowableUtil}. */
public class ThrowableUtilTest {


//    /** Tests {@link ThrowableUtil#throwIfUnchecked(Throwable)}. */
//    @Test
//    public void rethrowErrorOrException() throws Exception {
//        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrException(Error1.INSTANCE)).isSameAs(Error1.INSTANCE);
//        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrException(Exception1.INSTANCE)).isSameAs(Exception1.INSTANCE);
//        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrException(RuntimeException1.INSTANCE)).isSameAs(RuntimeException1.INSTANCE);
//        assertThat(ThrowableUtil.rethrowErrorOrException(Throwable1.INSTANCE)).isSameAs(Throwable1.INSTANCE);
//    }
//
//    /** Tests {@link ThrowableUtil#throwIfUnchecked(Throwable)}. */
//    @Test
//    public void rethrowErrorOrRuntimeException() {
//        assertThatThrownBy(() -> ThrowableUtil.throwIfUnchecked(Error1.INSTANCE)).isSameAs(Error1.INSTANCE);
//        assertThatThrownBy(() -> ThrowableUtil.throwIfUnchecked(RuntimeException1.INSTANCE)).isSameAs(RuntimeException1.INSTANCE);
//        assertThat(ThrowableUtil.throwIfUnchecked(Exception1.INSTANCE)).isSameAs(Exception1.INSTANCE);
//    }

    /** Tests {@link ThrowableUtil#throwAny(Throwable)}. */
    @Test
    public void throwAny() {
        assertThatThrownBy(() -> ThrowableUtil.throwAny(Exception1.INSTANCE)).isSameAs(Exception1.INSTANCE);
    }
}
