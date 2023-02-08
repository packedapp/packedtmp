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
package internal.app.packed.bean.hooks;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

import app.packed.bean.BeanHook.AnnotatedFieldHook;
import app.packed.extension.Extension;
import app.packed.util.Nullable;

/**
 *
 */
public record HookOnFieldAnnotation(Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable) {

    /** A cache of field annotations. */
    private static final ClassValue<HookOnFieldAnnotation> CACHE = new ClassValue<>() {

        @Override
        protected HookOnFieldAnnotation computeValue(Class<?> type) {
            AnnotatedFieldHook fieldHook = type.getAnnotation(AnnotatedFieldHook.class);
            if (fieldHook == null) {
                return null;
            }

            Utils.checkExtensionClass(type, fieldHook.extension());
            Utils.checkMemberAnnotation(type, ElementType.FIELD);

            return new HookOnFieldAnnotation(fieldHook.extension(), fieldHook.allowGet(), fieldHook.allowSet());
        }
    };

    @Nullable
    public static HookOnFieldAnnotation find(Class<? extends Annotation> annotationType) {
        return CACHE.get(annotationType);
    }
}
