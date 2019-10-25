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
import packed.internal.util.TinyPair;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public final class HookRequest {

    /** A list of custom hook callbacks for the particular extension. */
    final TinyPair<Hook, MethodHandle> customHooksCallback;

    final List<DelayedAnnotatedField> delayedFields;

    final List<DelayedAnnotatedMethod> delayedMethods;

    protected HookRequest(HookRequest.Builder builder) throws Throwable {
        this.customHooksCallback = builder.onHookModel.compute(builder.array);
        this.delayedMethods = builder.delayedMethods;
        this.delayedFields = builder.delayedFields;
    }

    public void invokeIt(Object target, Object additional) throws Throwable {
        // TODO support static....
        for (TinyPair<Hook, MethodHandle> c = customHooksCallback; c != null; c = c.next) {
            Hook hook = c.element1;
            MethodHandle mh = c.element2;
            if (mh.type().parameterCount() == 2) {
                mh.invoke(target, hook);
            } else {
                mh.invoke(target, hook, additional);
            }
        }
        for (DelayedAnnotatedField m : delayedFields) {
            try (HookTargetProcessor hp = new HookTargetProcessor(m.cp.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
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
            try (HookTargetProcessor hp = new HookTargetProcessor(m.cp.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
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

    public static final class Builder {

        final Object[] array;

        List<DelayedAnnotatedField> delayedFields = new ArrayList<>();

        List<DelayedAnnotatedMethod> delayedMethods = new ArrayList<>();

        final HookTargetProcessor hookProcessor;

        private final OnHookModel onHookModel;

        public Builder(OnHookModel model, HookTargetProcessor hookProcessor) {
            this.array = new Object[model.builderConstructors.length];
            this.onHookModel = requireNonNull(model);
            this.hookProcessor = requireNonNull(hookProcessor);
        }

        public HookRequest build() throws Throwable {
            return new HookRequest(this);
        }

        public void onAnnotatedField(Field field, Annotation annotation) throws Throwable {
            onHookModel.tryProcesAnnotatedField(this, field, annotation);
        }

        public void onAnnotatedMethod(Method method, Annotation annotation) throws Throwable {
            onHookModel.tryProcesAnnotatedMethod(this, method, annotation);
        }

        public void onAnnotatedType(Class<?> clazz, Annotation annotation) throws Throwable {
            throw new UnsupportedOperationException();
        }

        public Object singleConsume(ClassProcessor cp) throws Throwable {
            cp.findMethodsAndFields(onHookModel.allLinks.annotatedMethods == null ? null : f -> {
                for (Annotation a : f.getAnnotations()) {
                    onHookModel.tryProcesAnnotatedMethod(this, f, a);
                }
            }, onHookModel.allLinks.annotatedFields == null ? null : f -> {
                for (Annotation a : f.getAnnotations()) {
                    onHookModel.tryProcesAnnotatedField(this, f, a);
                }
            });
            onHookModel.compute(array);
            Object a = array[0];
            return a == null ? null : (((Hook.Builder<?>) a).build());
        }
    }

    static class DelayedAnnotatedField {
        final Annotation annotation;
        final ClassProcessor cp;
        final Field field;
        final MethodHandle mh;

        DelayedAnnotatedField(ClassProcessor cp, Field field, Annotation annotation, MethodHandle mh) {
            this.cp = requireNonNull(cp);
            this.field = requireNonNull(field);
            this.annotation = requireNonNull(annotation);
            this.mh = requireNonNull(mh);
        }

        public AnnotatedFieldHook<Annotation> toHook(HookTargetProcessor hookProcessor) {
            return ModuleAccess.hook().newAnnotatedFieldHook(hookProcessor, field, annotation);
        }
    }

    static class DelayedAnnotatedMethod {
        final Annotation annotation;
        final ClassProcessor cp;
        final Method method;
        final MethodHandle mh;

        DelayedAnnotatedMethod(ClassProcessor cp, Method method, Annotation annotation, MethodHandle mh) {
            this.cp = requireNonNull(cp);
            this.method = requireNonNull(method);
            this.annotation = requireNonNull(annotation);
            this.mh = requireNonNull(mh);
        }

        public AnnotatedMethodHook<Annotation> toHook(HookTargetProcessor hookProcessor) {
            return ModuleAccess.hook().newAnnotatedMethodHook(hookProcessor, method, annotation);
        }
    }
}
