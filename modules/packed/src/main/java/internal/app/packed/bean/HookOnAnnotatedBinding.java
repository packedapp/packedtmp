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
package internal.app.packed.bean;

import java.lang.annotation.Annotation;

import app.packed.extension.Extension;
import app.packed.extension.BeanHook.AnnotatedBindingHook;
import app.packed.util.Nullable;

/**
 *
 */
record HookOnAnnotatedBinding(Class<? extends Extension<?>> extensionType) {

    /** A cache of field annotations. */
    private static final ClassValue<HookOnAnnotatedBinding> CACHE = new ClassValue<>() {

        @Override
        protected HookOnAnnotatedBinding computeValue(Class<?> type) {
            AnnotatedBindingHook fieldHook = type.getAnnotation(AnnotatedBindingHook.class);
            if (fieldHook == null) {
                return null;
            }

            HookUtils.checkExtensionClass(type, fieldHook.extension());

            return new HookOnAnnotatedBinding(fieldHook.extension());
        }
    };

    @Nullable
    public static HookOnAnnotatedBinding find(Class<? extends Annotation> annotationType) {
        return CACHE.get(annotationType);
    }
}