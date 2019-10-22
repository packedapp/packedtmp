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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static testutil.util.TestMemberFinder.findField;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.WrongMethodTypeException;

import org.junit.jupiter.api.Test;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import features.hook.HookStubs.LeftAnnotatedFields;
import testutil.stubs.annotation.Left;
import testutil.stubs.annotation.Right;

/** Various tests related to hooks and annotated fields. */
public class AnnotatedFieldTest {

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
        assertThat(f.hooks).hasSize(1);
        AnnotatedFieldHook<Left> h = f.hooks.get(0);
        assertThat(h.annotation()).isInstanceOf(Left.class);
        assertThat(h.field().newField()).isEqualTo(findField(Tester.class, "ss2"));

        Tester t = new Tester();
        MethodHandle getter = f.getterOf(h);
        MethodHandle setter = f.setterOf(h);
        VarHandle varHandle = f.varHandleOf(h);

        // Test the getter
        assertThat(getter.type()).isSameAs(MethodType.methodType(String.class, Tester.class));
        assertThat(getter.invoke(t)).isEqualTo("gotIt");
        t.ss2 = "notBad";
        assertThat(getter.invoke(t)).isEqualTo("notBad");
        assertThatThrownBy(() -> getter.invoke()).isExactlyInstanceOf(WrongMethodTypeException.class);

        // Test the setter
        assertThat(t.ss2).isEqualTo("notBad");
        assertThat(setter.invoke(t, "fooBar")).isNull();
        assertThat(t.ss2).isEqualTo("fooBar");
        assertThat(getter.invoke(t)).isEqualTo("fooBar");
        assertThatThrownBy(() -> setter.invoke(t)).isExactlyInstanceOf(WrongMethodTypeException.class);
        assertThatThrownBy(() -> setter.invoke(t, 123)).isExactlyInstanceOf(WrongMethodTypeException.class);

        // Test the var handle
        assertThat(varHandle.get(t)).isEqualTo("fooBar");
        varHandle.set(t, "blabla");
        assertThat(t.ss2).isEqualTo("blabla");
    }

    @Test
    public void checks() {
        class Tester {
            @Left
            String ss2 = "gotIt";
        }
        AnnotatedFieldHook<Left> h = Hook.Builder.test(MethodHandles.lookup(), LeftAnnotatedFields.class, Tester.class).hooks.get(0);

        assertThat(h.checkAssignableTo(String.class)).isSameAs(h);
        assertThat(h.checkAssignableTo(CharSequence.class)).isSameAs(h);
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> h.checkAssignableTo(Long.class));

    }
}
