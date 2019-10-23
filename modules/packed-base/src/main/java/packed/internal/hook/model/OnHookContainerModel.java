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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.hook.model.OnHookContainerModelBuilder.LinkedEntry;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public final class OnHookContainerModel {

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> annotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> annotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> annotatedTypes;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> assignableTos;

    /** Constructors for each builder. */
    private final MethodHandle[] constructors;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    private final Link[] customHooks;

    OnHookContainerModel(OnHookContainerModelBuilder b) {
        this.annotatedFields = convert(b.hooks.annotatedFields);
        this.annotatedMethods = convert(b.hooks.annotatedMethods);
        this.annotatedTypes = convert(b.hooks.annotatedTypes);
        this.assignableTos = convert(b.hooks.assignableTos);

        this.customHooks = new Link[b.result.size()];
        this.constructors = new MethodHandle[b.result.size()];

        for (int i = 0; i < b.result.size(); i++) {
            OnHookContainerModelBuilder.Node n = b.result.get(i);
            constructors[i] = n.builderConstructor;
            if (b.hooks.customHooks != null) {
                // We reverse the order here so instead of Dependent->Dependency we get Dependency->Dependent
                // We do this so we do not automatically invoke methods on the root object. which is never cached.
                for (LinkedEntry l = b.hooks.customHooks.get(n.onNodeContainerType); l != null; l = l.next) {
                    customHooks[l.builder.index] = new Link(l.methodHandle, i, customHooks[l.builder.index]);
                }
            }
        }
    }

    /**
     * Returns an immutable set of all field triggering annotations types.
     * 
     * @return the set
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Set<Class<? extends Annotation>> annotatedFieldHookTypes() {
        Map<Class<?>, Link> a = annotatedFields;
        return a == null ? Set.of() : (Set) annotatedFields.keySet();
    }

    @Nullable
    public Object process(@Nullable Object parent, ClassProcessor cpTarget, UncheckedThrowableFactory<?> tf) {
        Object[] array = new Object[constructors.length];
        array[0] = parent;
        HookProcessor hc = new HookProcessor(cpTarget, tf);
        cpTarget.findMethodsAndFields(c -> {}, annotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                for (Link link = annotatedFields.get(a.annotationType()); link != null; link = link.next) {
                    Object builder = builderOf(this, link.index, array);
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

        // Process everything but the top elements, which we do in the end.
        for (int i = array.length - 1; i >= 0; i--) {
            for (Link link = customHooks[i]; link != null; link = link.next) {
                Object builder = builderOf(this, i, array);
                try {
                    link.mh.invoke(builder, array[link.index]);
                } catch (Throwable e1) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e1);
                    throw new UndeclaredThrowableException(e1);
                }
            }
            if (i > 0) {
                Object h = array[i];
                if (h != null) {
                    array[i] = ((Hook.Builder<?>) h).build();
                }
            }
        }
        if (parent != null) {
            return parent;
        }
        Object a = array[0];
        return a == null ? null : ((Hook.Builder<?>) a).build();
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
                l = new Link(e.methodHandle, e.builder.index, l);
            }
            return l;
        });

        return Map.copyOf(m);
    }

    private static Object builderOf(OnHookContainerModel m, int index, Object[] array) {
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

    private static class Link {
        private final int index;
        private final MethodHandle mh;

        @Nullable
        private final Link next;

        private Link(MethodHandle mh, int index, @Nullable Link next) {
            this.mh = requireNonNull(mh);
            this.index = index;
            this.next = next;
        }

    }
}
