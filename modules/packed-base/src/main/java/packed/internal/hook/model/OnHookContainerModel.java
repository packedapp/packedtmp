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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.hook.model.OnHookContainerModelBuilder.LinkedEntry;
import packed.internal.hook.model.OnHookContainerModelBuilder.OnHookContainerNode;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class OnHookContainerModel {

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    final Map<Class<?>, Link> onHookAnnotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    final Map<Class<?>, Link> onHookAnnotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
    final Map<Class<?>, Link> onHookAnnotatedTypes;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    final Map<Class<?>, Link> onHookAssignableTos;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    final Link[] onHookCustomHooks;

    final List<MethodHandle> constructors;

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
        onHookCustomHooks = new Link[b.sorted.size()];
        if (tmp != null) {
            for (int i = 0; i < b.sorted.size(); i++) {
                Class<? extends Hook> cl = b.sorted.get(i).hookType;
                onHookCustomHooks[i] = tmp.get(cl);
            }
        }
        this.constructors = List.copyOf(nodes);
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
                l = new Link(e.methodHandle, e.builder.id, l);
            }
            return l;
        });

        return Map.copyOf(m);
    }

    static class Link {
        final MethodHandle mh;
        final int index;

        @Nullable
        final Link next;

        Link(MethodHandle mh, int index, @Nullable Link next) {
            this.mh = requireNonNull(mh);
            this.index = index;
            this.next = next;
        }

        Object builderOf(OnHookContainerModel m, Object[] array) {
            Object builder = array[index];
            if (builder == null) {
                try {
                    builder = array[index] = m.constructors.get(index).invoke();
                } catch (Throwable e2) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e2);
                    throw new UndeclaredThrowableException(e2);
                }
            }
            return builder;
        }
    }
}
