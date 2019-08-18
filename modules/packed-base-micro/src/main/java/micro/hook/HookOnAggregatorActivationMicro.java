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

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.ActivateExtension;
import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.Extension;
import app.packed.container.extension.OnHook;
import app.packed.container.extension.OnHookAggregateBuilder;

/**
 *
 */
public class HookOnAggregatorActivationMicro {

    @ActivateExtension(HookActivateExtension.class)
    public @interface HookActivateAnnotation {}

    public static class HookActivateExtension extends Extension {

        @OnHook(SomeAggegator.class)
        public void process(ComponentConfiguration cc, String s) {}
    }

    public static class SomeAggegator implements OnHookAggregateBuilder<String> {

        /** {@inheritDoc} */
        @Override
        public String build() {
            return "ignore";
        }

        public void process(AnnotatedFieldHook<HookActivateAnnotation> hook) {}

        public void process(AnnotatedMethodHook<HookActivateAnnotation> hook) {}
    }
}
