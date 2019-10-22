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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import packed.internal.hook.HookProcessor;
import packed.internal.hook.model.OnHookContainerModelBuilder.LinkedEntry;
import packed.internal.hook.model.OnHookContainerModelBuilder.OnHookContainerNode;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public class UseIt {

    @SuppressWarnings("unchecked")
    public static <T extends Hook> T test(Lookup caller, Class<T> hookType, Class<?> target) {
        requireNonNull(caller, "caller is null");
        requireNonNull(hookType, "hookType is null");
        requireNonNull(target, "target is null");
        ClassProcessor cp = new ClassProcessor(caller, hookType, false);

        OnHookContainerModelBuilder ohs = new OnHookContainerModelBuilder(cp);
        ohs.process();

        ClassProcessor cpTarget = new ClassProcessor(caller, target, false);

        Object[] array = new Object[ohs.sorted.size()];

        cpTarget.findMethodsAndFields(c -> {}, f -> {
            if (ohs.onHookAnnotatedFields != null) {
                for (Annotation a : f.getAnnotations()) {
                    LinkedEntry e = ohs.onHookAnnotatedFields.get(a.annotationType());
                    while (e != null) {
                        Object builder = array[e.builder.id];
                        if (builder == null) {
                            try {
                                builder = array[e.builder.id] = e.builder.constructor.invoke();
                            } catch (Throwable e2) {
                                ThrowableUtil.rethrowErrorOrRuntimeException(e2);
                                throw new UndeclaredThrowableException(e2);
                            }
                        }

                        // e.builder.cp
                        try (HookProcessor hc = new HookProcessor(cpTarget, UncheckedThrowableFactory.ASSERTION_ERROR)) {
                            AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(hc, f, a);
                            try {
                                e.methodHandle.invoke(builder, hook);
                            } catch (Throwable e1) {
                                ThrowableUtil.rethrowErrorOrRuntimeException(e1);
                                throw new UndeclaredThrowableException(e1);
                            }
                        }
                        e = e.next;
                    }
                }
            }
        });

        for (int i = array.length - 1; i >= 0; i--) {
            Object h = array[i];
            if (h != null) {
                array[i] = ((Hook.Builder<?>) h).build();
            }
            OnHookContainerNode ocn = ohs.sorted.get(i);
            if (ohs.onHookCustomHooks != null) {
                LinkedEntry e = ohs.onHookCustomHooks.get(ocn.hookType);
                while (e != null) {
                    Object builder = array[e.builder.id];
                    if (builder == null) {
                        try {
                            builder = array[e.builder.id] = e.builder.constructor.invoke();
                        } catch (Throwable e2) {
                            ThrowableUtil.rethrowErrorOrRuntimeException(e2);
                            throw new UndeclaredThrowableException(e2);
                        }
                    }
                    // requireNonNull(result[i]);
                    try {
                        e.methodHandle.invoke(builder, array[i]);
                    } catch (Throwable e1) {
                        ThrowableUtil.rethrowErrorOrRuntimeException(e1);
                        throw new UndeclaredThrowableException(e1);
                    }
                    e = e.next;
                }
            }
        }
        return (T) array[0];

    }

}
