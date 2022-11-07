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
import app.packed.bean.CustomHook;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;

/**
 *
 */
public final class AssemblyMetaModel {

    private static final ClassValue<AssemblyMetaModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyMetaModel computeValue(Class<?> type) {
            if (type == Object.class) {
                return new AssemblyMetaModel();
            }
            AssemblyMetaModel parent = AssemblyMetaModel.of(type.getSuperclass());
            Annotation[] meta = type.getDeclaredAnnotations();
            if (meta.length == 0) {
                return parent;
            }

            return new AssemblyMetaModel(parent, meta);
        }
    };

    private final Map<String, Class<? extends Annotation>> bindings;

    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<ParameterAnnotationCache> PARAMETER_CACHE = new ClassValue<>() {

        @Override
        protected ParameterAnnotationCache computeValue(Class<?> type) {
            BindingHook h = type.getAnnotation(BindingHook.class);

            Class<? extends Annotation> cl = bindings.get(type.getName());
            if (cl != null) {
                Class<?> declaringClass = cl.getDeclaringClass();
                if (!Extension.class.isAssignableFrom(declaringClass)) {
                    throw new InternalExtensionException("oops");
                }
                @SuppressWarnings("unchecked")
                Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) declaringClass;
                return new ParameterAnnotationCache(extensionClass);
            }
            
            if (h == null) {
                return null;
            }

            // checkExtensionClass(type, h.extension());
            return new ParameterAnnotationCache(h.extension());
        }
    };

    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<FieldRecord> FIELD_ANNOTATION_CACHE = new ClassValue<>() {

        @Override
        protected FieldRecord computeValue(Class<?> type) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationType = (Class<? extends Annotation>) type;
            FieldHook fieldHook = type.getAnnotation(FieldHook.class);
            BindingHook provisionHook = type.getAnnotation(BindingHook.class);

            Class<? extends Annotation> cl = bindings.get(type.getName());
            if (cl != null) {
                Class<?> declaringClass = cl.getDeclaringClass();
                if (!Extension.class.isAssignableFrom(declaringClass)) {
                    throw new InternalExtensionException("oops");
                }
                @SuppressWarnings("unchecked")
                Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) declaringClass;
                return new FieldRecord(annotationType, extensionClass, true, true, false);
            }

            // assembly.meta.getMetaAnnotation

            if (provisionHook == fieldHook) { // check both null
                return null;
            } else if (provisionHook == null) {
                IntrospectedBean.checkExtensionClass(type, fieldHook.extension());
                return new FieldRecord(annotationType, fieldHook.extension(), fieldHook.allowGet(), fieldHook.allowSet(), false);
            } else if (fieldHook == null) {
                IntrospectedBean.checkExtensionClass(type, provisionHook.extension());
                return new FieldRecord(annotationType, provisionHook.extension(), false, true, true);
            } else {
                throw new InternalExtensionException(type + " cannot both be annotated with " + FieldHook.class + " and " + BindingHook.class);
            }
        }
    };
    
    final Map<String, String> fieldOrBindingHook = Map.of();


    // Tror ikke vi skal bruge den til noget
    @Nullable
    private final AssemblyMetaModel parent;

    private AssemblyMetaModel() {
        this.parent = null;
        this.bindings = Map.of();
    }

    private AssemblyMetaModel(AssemblyMetaModel parent, Annotation[] annotations) {
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

    FieldRecord lookupFieldAnnotation(Class<? extends Annotation> fieldAnnotation) {
        return FIELD_ANNOTATION_CACHE.get(fieldAnnotation);
    }

    ParameterAnnotationCache lookupParameterCache(Class<?> fieldAnnotation) {
        return PARAMETER_CACHE.get(fieldAnnotation);
    }

    public static AssemblyMetaModel of(Class<?> clazz) {
        return MODELS.get(clazz);
    }

    record ParameterAnnotationCache(Class<? extends Extension<?>> extensionType) {

    }

    record FieldRecord(Class<? extends Annotation> annotationType, Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable,
            boolean isProvision) {}
}
