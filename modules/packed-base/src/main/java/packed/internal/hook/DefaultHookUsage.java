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

import java.lang.annotation.Annotation;
import java.util.Set;

import app.packed.lang.Nullable;

/**
 *
 */
public class DefaultHookUsage {

    public final Set<Class<? extends Annotation>> annotatedFields;

    public final Set<Class<? extends Annotation>> annotatedMethods;

    public final Set<Class<? extends Annotation>> annotatedTypes;

    public final Set<Class<?>> assignableTos;

    DefaultHookUsage(Set<Class<? extends Annotation>> annotatedFields, Set<Class<? extends Annotation>> annotatedMethods,
            Set<Class<? extends Annotation>> annotatedTypes, Set<Class<?>> assignableTos) {
        this.annotatedFields = annotatedFields;
        this.annotatedMethods = annotatedMethods;
        this.annotatedTypes = annotatedTypes;
        this.assignableTos = assignableTos;
    }

    @Nullable
    public static DefaultHookUsage ofOrNull(@Nullable Set<Class<? extends Annotation>> annotatedFields,
            @Nullable Set<Class<? extends Annotation>> annotatedMethods, @Nullable Set<Class<? extends Annotation>> annotatedTypes,
            @Nullable Set<Class<?>> assignableTos) {
        if (annotatedFields == null && annotatedMethods == null && annotatedTypes == null && assignableTos == null) {
            return null;
        }
        return new DefaultHookUsage(annotatedFields, annotatedMethods, annotatedTypes, assignableTos);
    }
}
