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
package packed.internal.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public class HookRequest {

    /** A list of custom hook callbacks for the particular extension. */
    final CachedHook<Hook> customHooksCallback;

    List<DelayedAnnotatedMethod> delayedMethods;

    List<DelayedAnnotatedField> delayedFields;

    protected HookRequest(HookRequest.Builder builder) throws Throwable {
        this.customHooksCallback = builder.hooks.compute(builder.array);
        this.delayedMethods = builder.delayedMethods;
        this.delayedFields = builder.delayedFields;
    }

    protected void invokeIt(Object target, Object additional) throws Throwable {
        for (CachedHook<Hook> c = customHooksCallback; c != null; c = c.next()) {
            MethodHandle mh = c.mh();
            if (mh.type().parameterCount() == 2) {
                mh.invoke(target, c.hook());
            } else {
                mh.invoke(target, c.hook(), additional);
            }
        }
        for (DelayedAnnotatedField m : delayedFields) {
            try (HookProcessor hp = new HookProcessor(m.cp.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                MethodHandle mh = m.mh;
                AnnotatedFieldHook<Annotation> amh = ModuleAccess.hook().newAnnotatedFieldHook(hp, m.field, m.annotation);
                if (mh.type().parameterCount() == 2) {
                    mh.invoke(target, amh);
                } else {
                    mh.invoke(target, amh, additional);
                }
            }
        }
        for (DelayedAnnotatedMethod m : delayedMethods) {
            try (HookProcessor hp = new HookProcessor(m.cp.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                MethodHandle mh = m.mh;
                AnnotatedMethodHook<Annotation> amh = ModuleAccess.hook().newAnnotatedMethodHook(hp, m.method, m.annotation);
                if (mh.type().parameterCount() == 2) {
                    mh.invoke(target, amh);
                } else {
                    mh.invoke(target, amh, additional);
                }
            }
        }
    }

    public static class Builder {

        final Object[] array;

        List<DelayedAnnotatedMethod> delayedMethods = new ArrayList<>();

        List<DelayedAnnotatedField> delayedFields = new ArrayList<>();

        final OnHookContainerModel hooks;

        final HookProcessor hookProcessor;

        protected Builder(OnHookContainerModel model, HookProcessor hookProcessor) {
            this.array = new Object[model.size()];
            this.hooks = requireNonNull(model);
            this.hookProcessor = requireNonNull(hookProcessor);
        }

        private void onAnnotatedField(HookProcessor hookProcessor, Field field, Annotation annotation) throws Throwable {
            hooks.tryProcesAnnotatedField(hookProcessor, field, annotation, this);
        }

        private void onAnnotatedMethod(HookProcessor hookProcessor, Method method, Annotation annotation) throws Throwable {
            hooks.tryProcesAnnotatedMethod(hookProcessor, method, annotation, this);
        }

        public void onAnnotatedField(Field field, Annotation annotation) throws Throwable {
            onAnnotatedField(hookProcessor, field, annotation);
        }

        public void onAnnotatedMethod(Method method, Annotation annotation) throws Throwable {
            onAnnotatedMethod(hookProcessor, method, annotation);
        }
    }

    static class DelayedAnnotatedField {
        final Annotation annotation;
        final Field field;
        public final MethodHandle mh;
        final ClassProcessor cp;

        DelayedAnnotatedField(ClassProcessor cp, Field field, Annotation annotation, MethodHandle mh) {
            this.cp = requireNonNull(cp);
            this.field = requireNonNull(field);
            this.annotation = requireNonNull(annotation);
            this.mh = requireNonNull(mh);
        }

        public AnnotatedFieldHook<Annotation> toHook(HookProcessor hookProcessor) {
            return ModuleAccess.hook().newAnnotatedFieldHook(hookProcessor, field, annotation);
        }
    }

    static class DelayedAnnotatedMethod {
        final Annotation annotation;
        final Method method;
        public final MethodHandle mh;
        final ClassProcessor cp;

        DelayedAnnotatedMethod(ClassProcessor cp, Method method, Annotation annotation, MethodHandle mh) {
            this.cp = requireNonNull(cp);
            this.method = requireNonNull(method);
            this.annotation = requireNonNull(annotation);
            this.mh = requireNonNull(mh);
        }

        public AnnotatedMethodHook<Annotation> toHook(HookProcessor hookProcessor) {
            return ModuleAccess.hook().newAnnotatedMethodHook(hookProcessor, method, annotation);
        }
    }
}
