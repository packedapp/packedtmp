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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;

/**
 *
 */
final class MutableOnHookMap<V> {

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    IdentityHashMap<Class<?>, V> annotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    IdentityHashMap<Class<?>, V> annotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
    IdentityHashMap<Class<?>, V> annotatedTypes;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    IdentityHashMap<Class<?>, V> assignableTos;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    IdentityHashMap<Class<?>, V> customHooks;

    public IdentityHashMap<Class<?>, V> annotatedFieldsLazyInit() {
        IdentityHashMap<Class<?>, V> a = annotatedFields;
        if (a == null) {
            a = annotatedFields = new IdentityHashMap<>(1);
        }
        return a;
    }

    public boolean isEmpty() {
        return annotatedFields == null && annotatedMethods == null && annotatedTypes == null && assignableTos == null && customHooks == null;
    }

    public IdentityHashMap<Class<?>, V> annotatedMethodsLazyInit() {
        IdentityHashMap<Class<?>, V> a = annotatedMethods;
        if (a == null) {
            a = annotatedMethods = new IdentityHashMap<>(1);
        }
        return a;
    }

    public IdentityHashMap<Class<?>, V> annotatedTypesLazyInit() {
        IdentityHashMap<Class<?>, V> a = annotatedTypes;
        if (a == null) {
            a = annotatedTypes = new IdentityHashMap<>(1);
        }
        return a;
    }

    public IdentityHashMap<Class<?>, V> assignableTosLazyInit() {
        IdentityHashMap<Class<?>, V> a = assignableTos;
        if (a == null) {
            a = assignableTos = new IdentityHashMap<>(1);
        }
        return a;
    }

    public IdentityHashMap<Class<?>, V> customHooksLazyInit() {
        IdentityHashMap<Class<?>, V> a = customHooks;
        if (a == null) {
            a = customHooks = new IdentityHashMap<>(1);
        }
        return a;
    }

    public <E> ImmutableOnHookMap<E> toImmutable(Function<V, E> converter) {
        if (isEmpty()) {
            return null;
        }
        Map<Class<?>, E> annotatedFieldHooks = convert(annotatedFields, converter);
        Map<Class<?>, E> annotatedMethoddHooks = convert(annotatedMethods, converter);
        Map<Class<?>, E> annotatedTypeHooks = convert(annotatedTypes, converter);
        Map<Class<?>, E> assignableToHooks = convert(assignableTos, converter);
        return new ImmutableOnHookMap<E>(annotatedFieldHooks, annotatedMethoddHooks, annotatedTypeHooks, assignableToHooks);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <VV, E> Map<Class<?>, E> convert(IdentityHashMap<Class<?>, VV> map, Function<VV, E> f) {
        if (map == null) {
            return null;
        }
        // Replace in map
        IdentityHashMap m = map;

        m.replaceAll((k, v) -> ((Function) f).apply(v));

        return Map.copyOf(m);
    }
}
