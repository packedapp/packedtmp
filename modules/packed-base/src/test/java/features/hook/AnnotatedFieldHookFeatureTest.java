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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import features.hook.HookStubs.LeftAnnotatedFields;
import testutil.stubs.annotation.Left;
import testutil.stubs.annotation.Right;

/** Various tests related to hooks and annotated fields. */
public class AnnotatedFieldHookFeatureTest {

    @Test
    public void noActivatedHooksReturnNull() {
        class Tester {}
        assertThat(Hook.Builder.test(MethodHandles.lookup(), LeftAnnotatedFields.class, Tester.class)).isNull();
    }

    @Test
    public void nonActivatingField() {
        class Tester {
            @Right
            static final String ss1 = "nope";
            @Right
            final String ss2 = "alsoNope";
        }
        assertThat(Hook.Builder.test(MethodHandles.lookup(), LeftAnnotatedFields.class, Tester.class)).isNull();
    }

    @Test
    public void singleField() throws Throwable {
        class Tester {
            @Left
            String ss2 = "gotIt";
        }
        LeftAnnotatedFields f = Hook.Builder.test(MethodHandles.lookup(), LeftAnnotatedFields.class, Tester.class);
        assertThat(f.fields).hasSize(1);

        AnnotatedFieldHook<Left> h = f.fields.get(0);
        assertThat(h.annotation()).isInstanceOf(Left.class);
        // assertThat(h.field().newField()).isEqualTo(findField(Tester.class, "ss2"));

        // These methods cannot be called after the hook has been created.
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.getter());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.setter());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.varHandle());
    }
}
