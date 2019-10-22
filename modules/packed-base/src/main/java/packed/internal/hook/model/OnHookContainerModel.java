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
import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
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
public class OnHookContainerModel {

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    private final Map<Class<?>, Link> onHookAnnotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    final Map<Class<?>, Link> onHookAnnotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
    final Map<Class<?>, Link> onHookAnnotatedTypes;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    final Map<Class<?>, Link> onHookAssignableTos;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    private final Link[] onHookCustomHooks;

    /** Constructors for each builder. */
    private final MethodHandle[] constructors;

    OnHookContainerModel(OnHookContainerModelBuilder b) {
        ArrayList<MethodHandle> nodes = new ArrayList<>();
        for (OnHookContainerNode e : b.sorted) {
            nodes.add(e.constructor);
        }
        this.onHookAnnotatedFields = convert(b.onHookAnnotatedFields);
        this.onHookAnnotatedMethods = convert(b.onHookAnnotatedMethods);
        this.onHookAnnotatedTypes = convert(b.onHookAnnotatedTypes);
        this.onHookAssignableTos = convert(b.onHookAssignableTos);
        Map<Class<?>, Link> tmp = convert(b.onHookCustomHooks);

        this.onHookCustomHooks = new Link[b.sorted.size()];
        this.constructors = new MethodHandle[b.sorted.size()];
        for (int i = 0; i < b.sorted.size(); i++) {
            OnHookContainerNode n = b.sorted.get(i);
            constructors[i] = n.constructor;
            if (tmp != null) {
                onHookCustomHooks[i] = tmp.get(n.hookType);
            }
        }
    }

    public Object process(ClassProcessor cpTarget, UncheckedThrowableFactory<?> tf) {
        Object[] array = new Object[constructors.length];
        HookProcessor hc = new HookProcessor(cpTarget, tf);
        cpTarget.findMethodsAndFields(c -> {}, onHookAnnotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                for (Link link = onHookAnnotatedFields.get(a.annotationType()); link != null; link = link.next) {
                    Object builder = link.builderOf(this, array);
                    AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(hc, f, a);
                    try {
                        link.mh.invoke(builder, hook);
                    } catch (Throwable e) {
                        ThrowableUtil.rethrowErrorOrRuntimeException(e);
                        throw new UndeclaredThrowableException(e);
                    }
                }
            }
        });
        hc.close();

        for (int i = array.length - 1; i >= 0; i--) {
            Object h = array[i];
            if (h != null) {
                array[i] = ((Hook.Builder<?>) h).build();
                for (Link link = onHookCustomHooks[i]; link != null; link = link.next) {
                    Object builder = link.builderOf(this, array);
                    try {
                        link.mh.invoke(builder, array[i]);
                    } catch (Throwable e1) {
                        ThrowableUtil.rethrowErrorOrRuntimeException(e1);
                        throw new UndeclaredThrowableException(e1);
                    }
                }
            }
        }
        return array[0];
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map<Class<?>, Link> convert(IdentityHashMap<Class<?>, OnHookContainerModelBuilder.LinkedEntry> map) {
        if (map == null) {
            return null;
        }
        // Replace in map
        IdentityHashMap m = map;

        m.replaceAll((k, v) -> {
            OnHookContainerModelBuilder.LinkedEntry e = (LinkedEntry) v;
            Link l = null;
            for (; e != null; e = e.next) {
                l = new Link(e, l);
            }
            return l;
        });

        return Map.copyOf(m);
    }

    private static class Link {
        private final MethodHandle mh;
        private final int index;

        @Nullable
        private final Link next;

        private Link(LinkedEntry e, @Nullable Link next) {
            this.mh = requireNonNull(e.methodHandle);
            this.index = e.builder.id;
            this.next = next;
        }

        private Object builderOf(OnHookContainerModel m, Object[] array) {
            Object builder = array[index];
            if (builder == null) {
                try {
                    builder = array[index] = m.constructors[index].invoke();
                } catch (Throwable e2) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e2);
                    throw new UndeclaredThrowableException(e2);
                }
            }
            return builder;
        }
    }
}
