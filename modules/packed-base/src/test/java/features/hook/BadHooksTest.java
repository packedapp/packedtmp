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
package features.hook;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.hook.Hook;
import app.packed.hook.OnHook;

/** This class tests various illegal combinations of hooks that should fail. */
public class BadHooksTest {

    @Test
    public void hookCannotDependOnItSelf() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Hook.Builder.test(MethodHandles.lookup(), HookDependingOnItSelf.class, BadHooksTest.class));
    }

    static class HookDependingOnItSelf implements Hook {

        static class Builder implements Hook.Builder<HookDependingOnItSelf> {

            @OnHook
            public void on(HookDependingOnItSelf s) {}

            /** {@inheritDoc} */
            @Override
            public HookDependingOnItSelf build() {
                throw new AssertionError();
            }
        }
    }
}
