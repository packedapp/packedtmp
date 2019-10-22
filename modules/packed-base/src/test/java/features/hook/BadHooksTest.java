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

    /** Tests that at least one method annotated with {@link OnHook} is available. */
    @Test
    public void noHookMethods() {
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> Hook.Builder.test(MethodHandles.lookup(), NoHookMethods.class, BadHooksTest.class));
    }

    @Test
    public void hookCannotDependOnItSelf() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> Hook.Builder.test(MethodHandles.lookup(), HookDependingOnItSelf.class, BadHooksTest.class));
    }

    public static class NoHookMethodsOnSubHook implements Hook {

        static class Builder implements Hook.Builder<NoHookMethodsOnSubHook> {

            /** {@inheritDoc} */
            @Override
            public NoHookMethodsOnSubHook build() {
                throw new AssertionError();
            }
        }
    }

    public static class NoHookMethods implements Hook {

        static class Builder implements Hook.Builder<NoHookMethods> {

            /** {@inheritDoc} */
            @Override
            public NoHookMethods build() {
                throw new AssertionError();
            }
        }
    }

    static class HookDependingOnItSelf implements Hook {

        static class Builder implements Hook.Builder<HookDependingOnItSelf> {

            /** {@inheritDoc} */
            @Override
            public HookDependingOnItSelf build() {
                throw new AssertionError();
            }

            @OnHook
            public void on(HookDependingOnItSelf s) {}
        }
    }
}
