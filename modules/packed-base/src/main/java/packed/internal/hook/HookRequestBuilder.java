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

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import packed.internal.hook.HookRequest.DelayedAnnotatedMember;
import packed.internal.hook.OnHookModel.Link;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.Tiny;
import packed.internal.util.TinyPair;

/**
 *
 */
public class HookRequestBuilder {

    final Object[] array;

    Tiny<DelayedAnnotatedMember> delayedMembers;

    final HookTargetProcessor hookProcessor;

    final OnHookModel onHookModel;

    final boolean isTest;

    public HookRequestBuilder(OnHookModel model, HookTargetProcessor hookProcessor) {
        this(model, hookProcessor, false);
    }

    public HookRequestBuilder(OnHookModel model, HookTargetProcessor hookProcessor, boolean isTest) {
        this.array = new Object[model.builderConstructors.length];
        this.onHookModel = requireNonNull(model);
        this.hookProcessor = requireNonNull(hookProcessor);
        this.isTest = isTest;
    }

    public HookRequest build() throws Throwable {
        return new HookRequest(this);
    }

    private Hook.Builder<?> builderOf(Object[] array, int index) throws Throwable {
        Object builder = array[index];
        if (builder == null) {
            builder = array[index] = onHookModel.builderConstructors[index].invoke();
        }
        return (Hook.Builder<?>) builder;
    }

    TinyPair<Hook, MethodHandle> compute() throws Throwable {
        // This code is same as process()
        for (int i = array.length - 1; i >= 0; i--) {
            for (Link link = onHookModel.customHooks[i]; link != null; link = link.next) {
                if (onHookModel.builderConstructors[i] != null) {
                    Hook.Builder<?> builder = builderOf(array, i);
                    link.mh.invoke(builder, array[link.index]);
                }
            }
            if (i > 0) {
                Object h = array[i];
                if (h != null) {
                    array[i] = ((Hook.Builder<?>) h).build();
                }
            }
        }

        TinyPair<Hook, MethodHandle> result = null;
        for (Link link = onHookModel.customHooks[0]; link != null; link = link.next) {
            result = new TinyPair<>((Hook) array[link.index], link.mh, result);
        }
        return result;
    }

    public void onAnnotatedField(Field field, Annotation annotation) throws Throwable {
        for (Link link = onHookModel.allLinks.annotatedFields.get(annotation.annotationType()); link != null; link = link.next) {
            if (link.index == 0 && !isTest) {
                delayedMembers = new Tiny<>(new DelayedAnnotatedMember(field, annotation, link.mh), delayedMembers);
            } else {
                Hook.Builder<?> builder = builderOf(array, link.index);
                AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(hookProcessor, field, annotation);
                if (link.mh.type().parameterCount() == 1) {
                    link.mh.invoke(hook);
                } else {
                    link.mh.invoke(builder, hook);
                }
            }
        }
    }

    public void onAnnotatedMethod(Method method, Annotation annotation) throws Throwable {
        for (Link link = onHookModel.allLinks.annotatedMethods.get(annotation.annotationType()); link != null; link = link.next) {
            if (link.index == 0 && !isTest) {
                delayedMembers = new Tiny<>(new DelayedAnnotatedMember(method, annotation, link.mh), delayedMembers);
            } else {
                Hook.Builder<?> builder = builderOf(array, link.index);
                AnnotatedMethodHook<Annotation> hook = ModuleAccess.hook().newAnnotatedMethodHook(hookProcessor, method, annotation);
                link.mh.invoke(builder, hook);
            }
        }
    }

    public void onAnnotatedType(Class<?> clazz, Annotation annotation) throws Throwable {
        throw new UnsupportedOperationException();
    }

    public Object singleConsume(ClassProcessor cp) throws Throwable {
        cp.findMethodsAndFields(onHookModel.allLinks.annotatedMethods == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                onAnnotatedMethod(f, a);
            }
        }, onHookModel.allLinks.annotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                onAnnotatedField(f, a);
            }
        });
        compute();
        Object a = array[0];
        return a == null ? null : (((Hook.Builder<?>) a).build());
    }
}
