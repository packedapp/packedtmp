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
package app.packed.operation.op;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import testutil.stubs.Throwables.Exception1;

/** Tests {@link OpException}. */
public class FactoryExceptionTest {

    /** Tests the various constructors.  */
    @Test
    public void test() {
        assertThat(new OpException("foo")).hasNoCause();
        assertThat(new OpException("foo")).hasMessage("foo");
        assertThat(new OpException("foobar", Exception1.INSTANCE)).hasCause(Exception1.INSTANCE);
        assertThat(new OpException("foobar", Exception1.INSTANCE)).hasMessage("foobar");
    }
}
