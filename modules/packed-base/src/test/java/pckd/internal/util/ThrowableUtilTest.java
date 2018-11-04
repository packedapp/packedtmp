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
package pckd.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import pckd.internal.util.ThrowableUtil;
import support.stubs.Throwables.Error1;
import support.stubs.Throwables.Exception1;
import support.stubs.Throwables.RuntimeException1;
import support.stubs.Throwables.Throwable1;

/** Tests {@link ThrowableUtil}. */
public class ThrowableUtilTest {

    /** Tests {@link ThrowableUtil#fromCompletionFuture(CompletableFuture)}. */
    @Test
    public void fromCompletionFuture() {
        assertThat(ThrowableUtil.fromCompletionFuture(new CompletableFuture<>())).isNull();

        CompletableFuture<String> cf = new CompletableFuture<>();
        cf.complete("foo");
        assertThat(ThrowableUtil.fromCompletionFuture(cf)).isNull();

        cf = new CompletableFuture<>();
        cf.completeExceptionally(Throwable1.INSTANCE);
        assertThat(ThrowableUtil.fromCompletionFuture(cf)).isEqualTo(Throwable1.INSTANCE);

        cf = new CompletableFuture<>();
        cf.cancel(true);
        assertThat(ThrowableUtil.fromCompletionFuture(cf)).isInstanceOf(CancellationException.class);
    }

    /** Tests {@link ThrowableUtil#rethrowErrorOrRuntimeException(Throwable)}. */
    @Test
    public void rethrowErrorOrException() throws Exception {
        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrException(Error1.INSTANCE)).isSameAs(Error1.INSTANCE);
        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrException(Exception1.INSTANCE)).isSameAs(Exception1.INSTANCE);
        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrException(RuntimeException1.INSTANCE)).isSameAs(RuntimeException1.INSTANCE);
        assertThat(ThrowableUtil.rethrowErrorOrException(Throwable1.INSTANCE)).isSameAs(Throwable1.INSTANCE);
    }

    /** Tests {@link ThrowableUtil#rethrowErrorOrRuntimeException(Throwable)}. */
    @Test
    public void rethrowErrorOrRuntimeException() {
        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrRuntimeException(Error1.INSTANCE)).isSameAs(Error1.INSTANCE);
        assertThatThrownBy(() -> ThrowableUtil.rethrowErrorOrRuntimeException(RuntimeException1.INSTANCE)).isSameAs(RuntimeException1.INSTANCE);
        assertThat(ThrowableUtil.rethrowErrorOrRuntimeException(Exception1.INSTANCE)).isSameAs(Exception1.INSTANCE);
    }

    /** Tests {@link ThrowableUtil#throwAny(Throwable)}. */
    @Test
    public void throwAny() {
        assertThatThrownBy(() -> ThrowableUtil.throwAny(Exception1.INSTANCE)).isSameAs(Exception1.INSTANCE);
    }
}
