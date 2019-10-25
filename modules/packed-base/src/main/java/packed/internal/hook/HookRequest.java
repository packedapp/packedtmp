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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    final List<DelayedAnnotatedMember<Field>> delayedFields;

    final List<DelayedAnnotatedMember<Method>> delayedMethods;

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
        for (DelayedAnnotatedMember<Field> m : delayedFields) {
            try (HookTargetProcessor hp = new HookTargetProcessor(m.cp.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                MethodHandle mh = m.mh;
                Hook amh = ModuleAccess.hook().newAnnotatedFieldHook(hp, m.member, m.annotation);
                if (mh.type().parameterCount() == 2) {
                    mh.invoke(target, amh);
                } else {
                    mh.invoke(target, amh, additional);
                }
            }
        }
        for (DelayedAnnotatedMember<Method> m : delayedMethods) {
            try (HookTargetProcessor hp = new HookTargetProcessor(m.cp.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                MethodHandle mh = m.mh;
                Hook amh = ModuleAccess.hook().newAnnotatedMethodHook(hp, m.member, m.annotation);
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

        List<DelayedAnnotatedMember<Field>> delayedFields = new ArrayList<>();

        List<DelayedAnnotatedMember<Method>> delayedMethods = new ArrayList<>();

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

    static class DelayedAnnotatedMember<T extends Member> {
        final Annotation annotation;
        final ClassProcessor cp;
        final T member;
        final MethodHandle mh;

        DelayedAnnotatedMember(ClassProcessor cp, T member, Annotation annotation, MethodHandle mh) {
            this.cp = requireNonNull(cp);
            this.member = requireNonNull(member);
            this.annotation = requireNonNull(annotation);
            this.mh = requireNonNull(mh);
        }
    }

}
