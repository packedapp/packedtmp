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
import java.util.ArrayList;
import java.util.List;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import testutil.stubs.annotation.Left;

/**
 *
 */
public class HookStubs {

    public static class Aggregate implements Hook {

        final LeftAnnotatedFields laf;

        Aggregate(LeftAnnotatedFields laf) {
            this.laf = laf;
        }

        static class Builder implements Hook.Builder<Aggregate> {

            private LeftAnnotatedFields laf;

            @OnHook
            public void on(LeftAnnotatedFields laf) {
                this.laf = laf;
            }

            /** {@inheritDoc} */
            @Override
            public Aggregate build() {
                return new Aggregate(laf);
            }
        }
    }

    public static class LeftAnnotatedFields implements Hook {

        /** All fields that was annotated with Left */
        public final List<AnnotatedFieldHook<Left>> fields;

        private LeftAnnotatedFields(Builder builder) {
            this.fields = List.copyOf(builder.fields);
        }

        private static class Builder implements Hook.Builder<LeftAnnotatedFields> {

            private final ArrayList<AnnotatedFieldHook<Left>> fields = new ArrayList<>();

            @OnHook
            private void foo(AnnotatedFieldHook<Left> hook) throws IllegalAccessException {
                // These methods cannot be called after build() has been invoked, so test them now
                assertThat(hook.getter().type()).isSameAs(hook.field().unreflectGetter(MethodHandles.lookup()).type());
                assertThat(hook.varHandle().varType()).isSameAs(hook.field().unreflectVarHandle(MethodHandles.lookup()).varType());

                if (hook.field().isFinal()) {
                    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> hook.setter());
                } else {
                    assertThat(hook.setter().type()).isSameAs(hook.field().unreflectSetter(MethodHandles.lookup()).type());
                    hook.setter();
                }
                fields.add(hook);
            }

            /** {@inheritDoc} */
            @Override
            public LeftAnnotatedFields build() {
                return new LeftAnnotatedFields(this);
            }
        }
    }
}
