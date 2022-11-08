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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Nullable;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.bean.BeanExtensionPoint.MethodHook;
import app.packed.bean.BeanIntrospector.OnBinding;
import app.packed.bean.BeanIntrospector.OnField;
import app.packed.bean.CustomHook;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import internal.app.packed.util.ClassUtil;

/**
 *
 */
public final class BeanHookModel {

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<AnnotatedMethod> ANNOTATED_METHOD_CACHE = new ClassValue<>() {

        @Override
        protected AnnotatedMethod computeValue(Class<?> type) {
            MethodHook h = type.getAnnotation(MethodHook.class);
            if (h == null) {
                return null;
            }
            checkExtensionClass(type, h.extension());
            return new AnnotatedMethod(h.extension(), h.allowInvoke());
        }
    };

    private static final ClassValue<BeanHookModel> MODELS = new ClassValue<>() {

        @Override
        protected BeanHookModel computeValue(Class<?> type) {
            if (type == Object.class) {
                return new BeanHookModel();
            }
            BeanHookModel parent = BeanHookModel.of(type.getSuperclass());
            Annotation[] meta = type.getDeclaredAnnotations();
            if (meta.length == 0) {
                return parent;
            }

            return new BeanHookModel(parent, meta);
        }
    };

    private final Map<String, Class<? extends Annotation>> bindings;

    /** A cache of field annotations. */
    private final ClassValue<AnnotatedField> FIELD_ANNOTATION_CACHE = new ClassValue<>() {

        @Override
        protected AnnotatedField computeValue(Class<?> type) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationType = (Class<? extends Annotation>) type;

            // Er det her en RAW thingy???
            // Der er ingen grund til vi laeser typen flere gange vel
            AnnotatedField result = null;

            FieldHook fieldHook = type.getAnnotation(FieldHook.class);
            if (fieldHook != null) {
                checkExtensionClass(type, fieldHook.extension());
                result = new AnnotatedField(fieldHook.extension(), fieldHook.allowGet(), fieldHook.allowSet(), false);
            }

            BindingHook bindingHook = type.getAnnotation(BindingHook.class);
            if (bindingHook != null) {
                if (result != null) {
                    throw new InternalExtensionException(annotationType + " cannot both be annotated with " + FieldHook.class + " and " + BindingHook.class);
                }
                checkExtensionClass(type, bindingHook.extension());
                result = new AnnotatedField(bindingHook.extension(), false, true, true);
            }

            // See if we have a custom hook
            CustomAnnotatedField customHook = fieldHooks.get(type.getName());

            if (customHook != null) {
                if (result != null) {
                    throw new InternalExtensionException("POOPS");
                }
                result = new AnnotatedField(extract(annotationType), customHook.allowGet, customHook.allowSet, customHook.isBindingHook);
            }

            return result;
        }
    };

    private final Map<String, CustomAnnotatedField> fieldHooks = Map.of();

    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<ParameterTypeRecord> PARAMETER_TYPE_CACHE = new ClassValue<>() {

        @Override
        protected ParameterTypeRecord computeValue(Class<?> type) {
            BindingHook h = type.getAnnotation(BindingHook.class);
            Class<? extends Annotation> cl = bindings.get(type.getName());
            if (cl != null) {
                Class<?> declaringClass = cl.getDeclaringClass();
                if (!Extension.class.isAssignableFrom(declaringClass)) {
                    throw new InternalExtensionException("oops");
                }
                @SuppressWarnings("unchecked")
                Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) declaringClass;
                return new ParameterTypeRecord(extensionClass);
            }

            if (h == null) {
                return null;
            }

            // checkExtensionClass(type, h.extension());
            return new ParameterTypeRecord(h.extension());
        }
    };

    // Tror ikke vi skal bruge den til noget
    @Nullable
    private final BeanHookModel parent;

    private BeanHookModel() {
        this.parent = null;
        this.bindings = Map.of();
    }

    private BeanHookModel(BeanHookModel parent, Annotation[] annotations) {
        this.parent = requireNonNull(parent);
        List<AssemblyMetaHolder> holders = new ArrayList<>();
        for (Annotation a : annotations) {
            if (a.annotationType().isAnnotationPresent(CustomHook.class)) {
                holders.add(new AssemblyMetaHolder(a.annotationType()));
            }
        }
        Map<String, Class<? extends Annotation>> bindings = new HashMap<>();
        for (AssemblyMetaHolder h : holders) {
            for (String s : h.bindings) {
                bindings.put(s, h.annotationType);
            }
        }
        this.bindings = Map.copyOf(bindings);
    }

    AnnotatedField lookupAnnotationOnField(Class<? extends Annotation> fieldAnnotation) {
        return FIELD_ANNOTATION_CACHE.get(fieldAnnotation);
    }

    AnnotatedMethod lookupAnnotationOnMethod(Class<? extends Annotation> fieldAnnotation) {
        return ANNOTATED_METHOD_CACHE.get(fieldAnnotation);
    }

    ParameterTypeRecord lookupParameterType(Class<?> parameterType) {
        return PARAMETER_TYPE_CACHE.get(parameterType);
    }

    static void checkExtensionClass(Class<?> annotationType, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, s -> new InternalExtensionException(s));
        if (extensionType.getModule() != annotationType.getModule()) {
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + extensionType + " must be declared in the same module");
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Extension<?>> extract(Class<? extends Annotation> annotationtype) {
        Class<?> declaringClass = annotationtype.getDeclaringClass();
        if (!Extension.class.isAssignableFrom(declaringClass)) {
            throw new InternalExtensionException("oops");
        }
        return (Class<? extends Extension<?>>) declaringClass;
    }

    public static BeanHookModel of(Class<?> clazz) {
        return MODELS.get(clazz);
    }

    private record CustomAnnotatedField(Class<? extends Annotation> annotationType, boolean isBindingHook, boolean allowGet, boolean allowSet) {}

    record AnnotatedMethod(Class<? extends Extension<?>> extensionType, boolean isInvokable) {}

    record ParameterTypeRecord(Class<? extends Extension<?>> extensionType) {}

    /**
     * A hook annotation on a field, is either a plain {@link OnBinding} hook or a {@link OnField} hook.
     */
    record AnnotatedField(Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable, boolean isBindingHook) {}
}
