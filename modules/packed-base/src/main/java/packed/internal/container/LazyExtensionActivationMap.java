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
package packed.internal.container;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.Packlet;
import app.packed.container.Extension;
import packed.internal.hook.BaseHookQualifierList;
import packed.internal.hook.OnHookModel;
import packed.internal.util.Tiny;

/**
 *
 */
public final class LazyExtensionActivationMap {

    /** A cache of any extensions a particular annotation activates. */
    public static final ClassValue<Set<Class<? extends Extension>>> EXTENSION_ACTIVATORS = new ClassValue<>() {

        @Override
        protected Set<Class<? extends Extension>> computeValue(Class<?> type) {
            Packlet ae = type.getAnnotation(Packlet.class);
            return ae == null ? null : Set.of(ae.extension());
        }
    };

    @Nullable
    private final Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedFields;

    @Nullable
    private final Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedMethods;

    @Nullable
    private final Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedTypes;

    @Nullable
    private final Map<Class<?>, Set<Class<? extends Extension>>> assignableTo;

    private LazyExtensionActivationMap(@Nullable Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedFields,
            @Nullable Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedMethods,
            @Nullable Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedTypes,
            @Nullable Map<Class<?>, Set<Class<? extends Extension>>> assignableTo) {
        this.annotatedFields = annotatedFields;
        this.annotatedMethods = annotatedMethods;
        this.annotatedTypes = annotatedTypes;
        this.assignableTo = assignableTo;
    }

    @Nullable
    public Set<Class<? extends Extension>> onAnnotatedField(Class<? extends Annotation> annotationType) {
        return annotatedFields == null ? null : annotatedFields.get(annotationType);
    }

    @Nullable
    public Set<Class<? extends Extension>> onAnnotatedMethod(Class<? extends Annotation> annotationType) {
        return annotatedMethods == null ? null : annotatedMethods.get(annotationType);
    }

    @Nullable
    public Set<Class<? extends Extension>> onAnnotatedType(Class<? extends Annotation> annotationType) {
        return annotatedTypes == null ? null : annotatedTypes.get(annotationType);
    }

    @Nullable
    public Set<Class<? extends Extension>> onAssignableTo(Class<?> type) {
        return assignableTo == null ? null : assignableTo.get(type);
    }

    @Nullable
    public static BaseHookQualifierList findNonExtending(OnHookModel hooks) {
        return BaseHookQualifierList.ofOrNull(findNonAutoExtending(hooks.annotatedFieldHooks()), findNonAutoExtending(hooks.annotatedMethodHooks()),
                findNonAutoExtending(hooks.annotatedTypeHooks()), findNonAutoExtending(hooks.assignableTos()));
    }

    @Nullable
    private static <T> Set<Class<? extends T>> findNonAutoExtending(Set<Class<? extends T>> set) {
        Tiny<Class<? extends T>> n = null;
        if (set != null) {
            for (Class<? extends T> c : set) {
                if (!LazyExtensionActivationMap.isAutoActivate(c)) {
                    n = new Tiny<>(c, n);
                }
            }
        }
        return Tiny.toSetOrNull(n);
    }

    public static boolean isAutoActivate(Class<?> clazz) {
        return EXTENSION_ACTIVATORS.get(clazz) != null;
    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface LazyExtensionUsage {

        String[] optional() default {};

        Class<? extends Extension>[] value();
    }

    public static LazyExtensionActivationMap of(Class<?> cl) {
        HashMap<Class<? extends Annotation>, Tiny<Class<? extends Extension>>> annotatedFields = new HashMap<>(0);
        HashMap<Class<? extends Annotation>, Tiny<Class<? extends Extension>>> annotatedMethods = new HashMap<>(0);
        HashMap<Class<? extends Annotation>, Tiny<Class<? extends Extension>>> annotatedTypes = new HashMap<>(0);
        HashMap<Class<?>, Tiny<Class<? extends Extension>>> assignableTos = new HashMap<>(0);

        LazyExtensionUsage uel = cl.getAnnotation(LazyExtensionUsage.class);
        if (uel != null) {
            for (Class<? extends Extension> c : uel.value()) {
                ExtensionModel em = ExtensionModel.of(c);
                BaseHookQualifierList dhu = em.hooksNonActivating;
                if (dhu != null) {
                    stats(c, annotatedFields, dhu.annotatedFields);
                    stats(c, annotatedMethods, dhu.annotatedMethods);
                    stats(c, annotatedTypes, dhu.annotatedTypes);
                    stats(c, assignableTos, dhu.assignableTos);
                }
            }
        }

        // we also need all OnHook on bundles which do not have activating annotations.
        // Problem is that if an Extension has an un-activated OnHook method.
        // We just ignore it. Because we just assume that are added via normal mechanisms...
        Packlet uela = cl.getAnnotation(Packlet.class);
        if (uela != null) {
            for (Class<? extends Extension> c : uela.extension()) {
                ExtensionModel em = ExtensionModel.of(c);
                BaseHookQualifierList dhu = em.hooksNonActivating;
                if (dhu != null) {
                    stats(c, annotatedFields, dhu.annotatedFields);
                    stats(c, annotatedMethods, dhu.annotatedMethods);
                    stats(c, annotatedTypes, dhu.annotatedTypes);
                    stats(c, assignableTos, dhu.assignableTos);
                }
            }
        }
        if (uela == null && uel == null) {
            return null;
        }

        if (annotatedFields.size() == 0 && annotatedMethods.size() == 0 && annotatedTypes.size() == 0 && assignableTos.size() == 0) {
            System.err.println("Why use " + uel + " or " + uela);
            return null;
        }
        return new LazyExtensionActivationMap(Tiny.toMultiSetMapOrNull(annotatedFields), Tiny.toMultiSetMapOrNull(annotatedMethods),
                Tiny.toMultiSetMapOrNull(annotatedTypes), Tiny.toMultiSetMapOrNull(assignableTos));
    }

    @Nullable
    private static <T> void stats(Class<? extends Extension> extensionType, HashMap<Class<? extends T>, Tiny<Class<? extends Extension>>> map,
            @Nullable Set<Class<? extends T>> set) {
        if (set != null) {
            for (Class<? extends T> c : set) {
                map.compute(c, (k, v) -> new Tiny<>(extensionType, v));
            }
        }
    }
}