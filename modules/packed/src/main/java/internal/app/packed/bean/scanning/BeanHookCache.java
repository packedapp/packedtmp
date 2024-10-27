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
package internal.app.packed.bean.scanning;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import app.packed.bean.scanning.BeanTrigger.OnAnnotatedField;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedVariable;
import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;
import internal.app.packed.util.types.ClassUtil;

/**
 * Cache of annotations and classes that make use of {@link BeanHook} annotations.
 */
final class BeanHookCache {

    /** A cache of field annotations. */
    private static final ClassValue<HookOnFieldAnnotation> ANNOTATED_FIELD_HOOK_CACHE = new ClassValue<>() {

        @Override
        protected HookOnFieldAnnotation computeValue(Class<?> type) {
            OnAnnotatedField hook = type.getAnnotation(OnAnnotatedField.class);
            if (hook == null) {
                return null;
            }
            checkExtensionClass(type, hook.extension());
            checkMemberAnnotation(type, ElementType.FIELD);
            return new HookOnFieldAnnotation(hook.extension(), hook.allowGet(), hook.allowSet());
        }
    };

    /** A cache of field annotations. */
    private static final ClassValue<HookOnAnnotatedBinding> ANNOTATED_VARIABLE_HOOK_CACHE = new ClassValue<>() {

        @Override
        protected HookOnAnnotatedBinding computeValue(Class<?> type) {
            OnAnnotatedVariable hook = type.getAnnotation(OnAnnotatedVariable.class);
            if (hook == null) {
                return null; // Annotation not annotated with AnnotatedVariableHook
            }

            // This funtionality should be shared between all hook caches.
            Target t = type.getAnnotation(Target.class);
            if (t == null) {
                throw new InternalExtensionException("");
            }
            Set<ElementType> expected = Set.of(ElementType.FIELD, ElementType.TYPE_USE, ElementType.PARAMETER);
            if (Collections.disjoint(expected, Set.of(t.value()))) {
                throw new InternalExtensionException("");
            }
            // TODO check Element Types on the annotation
            BeanHookCache.checkExtensionClass(type, hook.extension());

            return new HookOnAnnotatedBinding(hook.extension());
        }
    };

    private static void checkExtensionClass(Class<?> annotationType, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, s -> new InternalExtensionException(s));
        if (extensionType.getModule() != annotationType.getModule()) {
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + extensionType + " must be declared in the same module");
        }
    }

    private static void checkMemberAnnotation(Class<?> clazz, ElementType elementType) {
        Target target = clazz.getAnnotation(Target.class);
        if (target == null) {
            throw new InternalExtensionException("@ " + clazz.getSimpleName());
        }

        List<ElementType> of = List.of(target.value());
        if (!of.contains(ElementType.FIELD)) {
            throw new InternalExtensionException("");
        }
        if (of.contains(ElementType.TYPE_USE) || of.contains(ElementType.PARAMETER)) {
            throw new InternalExtensionException("");
        }

    }

    @Nullable
    static HookOnFieldAnnotation findAnnotatedOnFieldHook(Class<? extends Annotation> annotationType) {
        return ANNOTATED_FIELD_HOOK_CACHE.get(annotationType);
    }

    @Nullable
    static HookOnAnnotatedBinding findAnnotatedVariableHook(Class<? extends Annotation> annotationType) {
        return ANNOTATED_VARIABLE_HOOK_CACHE.get(annotationType);
    }

    /**
     *
     * Represents an annotated annotated with {@link AnnotatedVariableHook}.
     */
    record HookOnAnnotatedBinding(Class<? extends Extension<?>> extensionType) {

    }

    record HookOnFieldAnnotation(Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable) {}
}
