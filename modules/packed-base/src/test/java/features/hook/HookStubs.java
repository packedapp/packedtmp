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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import testutil.stubs.annotation.Left;

/**
 *
 */
public class HookStubs {

    public static class LeftAnnotatedFields implements Hook {

        /** All fields that was annotated with Left */
        public final List<AnnotatedFieldHook<Left>> hooks;

        private final Map<AnnotatedFieldHook<Left>, MethodHandle> setters;

        private final Map<AnnotatedFieldHook<Left>, MethodHandle> getters;
        private final Map<AnnotatedFieldHook<Left>, VarHandle> varHandles;

        public MethodHandle setterOf(AnnotatedFieldHook<Left> hook) {
            return setters.get(hook);
        }

        public MethodHandle getterOf(AnnotatedFieldHook<Left> hook) {
            return getters.get(hook);
        }

        public VarHandle varHandleOf(AnnotatedFieldHook<Left> hook) {
            return varHandles.get(hook);
        }

        private LeftAnnotatedFields(Builder builder) {
            this.hooks = List.copyOf(builder.hooks);
            this.setters = Map.copyOf(builder.setters);
            this.getters = Map.copyOf(builder.getters);
            this.varHandles = Map.copyOf(builder.varHandles);
        }

        private static class Builder implements Hook.Builder<LeftAnnotatedFields> {

            private final ArrayList<AnnotatedFieldHook<Left>> hooks = new ArrayList<>();

            private final HashMap<AnnotatedFieldHook<Left>, MethodHandle> setters = new HashMap<>();

            private final HashMap<AnnotatedFieldHook<Left>, MethodHandle> getters = new HashMap<>();

            private final HashMap<AnnotatedFieldHook<Left>, VarHandle> varHandles = new HashMap<>();

            @OnHook
            private void foo(AnnotatedFieldHook<Left> hook) {
                hooks.add(hook);

                // These methods cannot be called after build() has been invoked.
                getters.put(hook, hook.getter());
                varHandles.put(hook, hook.varHandle());

                if (hook.field().isFinal()) {
                    assertThatThrownBy(() -> hook.setter()).isExactlyInstanceOf(UnsupportedOperationException.class);
                } else {
                    setters.put(hook, hook.setter());
                }

            }

            /** {@inheritDoc} */
            @Override
            public LeftAnnotatedFields build() {
                return new LeftAnnotatedFields(this);
            }
        }
    }
}
