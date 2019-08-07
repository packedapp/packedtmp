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
package micro.hook;

import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ActivateExtension;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;

/**
 *
 */
public class HookOnAggregatorActivationMicro {

    @ActivateExtension(HookActivateExtension.class)
    public @interface HookActivateAnnotation {}

    public static class HookActivateExtension extends Extension {

        @OnHook(aggreateWith = SomeAggegator.class)
        public void process(ComponentConfiguration cc, String s) {}
    }

    public static class SomeAggegator implements Supplier<String> {

        /** {@inheritDoc} */
        @Override
        public String get() {
            return "ignore";
        }

        @OnHook
        public void process(AnnotatedFieldHook<HookActivateAnnotation> hook) {}

        @OnHook
        public void process(AnnotatedMethodHook<HookActivateAnnotation> hook) {}
    }
}
