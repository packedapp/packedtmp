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
import packed.internal.hook.model.OnHookContainerModel.Link;
import packed.internal.hook.model.OnHookContainerModelBuilder.LinkedEntry;
import packed.internal.hook.model.OnHookContainerModelBuilder.OnHookContainerNode;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public class UseIt2 {

    @SuppressWarnings("unchecked")
    public static <T extends Hook> T test(Lookup caller, Class<T> hookType, Class<?> target) {
        requireNonNull(caller, "caller is null");
        requireNonNull(hookType, "hookType is null");
        requireNonNull(target, "target is null");
        ClassProcessor cp = new ClassProcessor(caller, hookType, false);

        OnHookContainerModelBuilder ohs = new OnHookContainerModelBuilder(cp);
        ohs.process();

        OnHookContainerModel m = new OnHookContainerModel(ohs);

        ClassProcessor cpTarget = new ClassProcessor(caller, target, false);

        Object[] array = new Object[ohs.sorted.size()];

        HookProcessor hc = new HookProcessor(cpTarget, UncheckedThrowableFactory.ASSERTION_ERROR);
        cpTarget.findMethodsAndFields(c -> {}, m.onHookAnnotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                Link link = m.onHookAnnotatedFields.get(a.annotationType());
                while (link != null) {
                    Object builder = link.builder(m, array);

                    AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(hc, f, a);
                    try {
                        link.mh.invoke(builder, hook);
                    } catch (Throwable e) {
                        ThrowableUtil.rethrowErrorOrRuntimeException(e);
                        throw new UndeclaredThrowableException(e);
                    }
                    link = link.next;
                }
            }
        });
        hc.close();

        for (int i = array.length - 1; i >= 0; i--) {
            Object h = array[i];
            if (h != null) {
                array[i] = ((Hook.Builder<?>) h).build();

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
        }
        return (T) array[0];

    }

}
