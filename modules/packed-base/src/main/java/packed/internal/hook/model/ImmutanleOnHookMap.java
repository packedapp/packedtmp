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

import java.util.Map;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;

/**
 *
 */
public class ImmutanleOnHookMap<V> {

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    @Nullable
    final Map<Class<?>, V> annotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    @Nullable
    final Map<Class<?>, V> annotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
    @Nullable
    final Map<Class<?>, V> annotatedTypes;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    @Nullable
    final Map<Class<?>, V> assignableTos;

    ImmutanleOnHookMap(Map<Class<?>, V> annotatedFields, Map<Class<?>, V> annotatedMethod, Map<Class<?>, V> annotatedTypes, Map<Class<?>, V> assignableTos) {
        this.annotatedFields = annotatedFields;
        this.annotatedMethods = annotatedMethod;
        this.annotatedTypes = annotatedTypes;
        this.assignableTos = assignableTos;
    }
}
