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
package packed.internal.hook.model;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.hook.Hook;

/**
 *
 */
public class HookRequest {

    public static class Builder {

        final Object[] array;

        final OnHookContainerModel hooks;

        protected Builder(OnHookContainerModel model) {
            this.array = new Object[model.size()];
            this.hooks = requireNonNull(model);
        }

        public CachedHook<Hook> compute() throws Throwable {
            return hooks.compute(array);
        }

        public void onAnnotatedField(HookProcessor hookProcessor, Field field, Annotation annotation) throws Throwable {
            hooks.tryProcesAnnotatedField(hookProcessor, field, annotation, array);
        }

        public void onAnnotatedMethod(HookProcessor hookProcessor, Method method, Annotation annotation) throws Throwable {
            hooks.tryProcesAnnotatedMethod(hookProcessor, method, annotation, array);
        }
    }

    static class DelayedAnnotatedMethod {
        final Field field;
        final Annotation annotation;

        DelayedAnnotatedMethod(Field field, Annotation annotation) {
            this.field = requireNonNull(field);
            this.annotation = requireNonNull(annotation);
        }
    }
}
