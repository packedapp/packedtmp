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
package app.packed.hook;

import java.lang.annotation.Annotation;

import app.packed.lifecycle.OnStart;

/**
 *
 */
public class HookCallbacks {

    @OnStart(after = "WebServer Started")
    @ComponentHook(onFieldAnnotation = Deprecated.class)
    public void hookedMethod(HookedAnnotatedMethod<Deprecated> deprecatedMethod) {}

    @ComponentFieldHook(annotatedWith = Deprecated.class)
    public void hookedMethod2(ComponentField field) {}

    interface HookedAnnotatedMethod<T extends Annotation> {}

    @interface ComponentHook {
        Class<? extends Annotation> onMethodAnnotation() default Annotation.class;

        Class<? extends Annotation> onFieldAnnotation() default Annotation.class;

        Class<?> onType() default Class.class;

        String description() default "";
    }

    @interface Hook {
        String description() default "";

    }

    // 3 typer

    @interface ComponentFieldHook {
        // Altsaa det bliver for kompliceret mht til at beskrive en API hvis det ikke kun er annotated methods...
        Class<? extends Annotation> annotatedWith();
    }

    interface ComponentField {
        @interface Hook {
            Class<? extends Annotation> value();
        }
    }

    // ComponentField
    // ComponentMethod
    // ComponentInstance

    // @FieldHook
    // @MethodHook
    // @InstanceHook

    // @ComponentField.Hook

    // rawMethodHandle...
    // injectedMethodHandler...

}
