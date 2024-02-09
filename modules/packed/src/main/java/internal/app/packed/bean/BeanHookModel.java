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

import app.packed.extension.BeanCustomActivator;
import app.packed.extension.BeanClassActivator.AnnotatedBeanVariableActivator;
import app.packed.extension.BeanClassActivator.AnnotatedBeanMethodActivator;
import app.packed.extension.BeanClassActivator.BindingClassActivator;
import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;
import internal.app.packed.util.types.ClassUtil;

/**
 *
 */
public final class BeanHookModel {

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<AnnotatedMethod> METHOD_ANNOTATION_CACHE = new ClassValue<>() {

        @Override
        protected AnnotatedMethod computeValue(Class<?> type) {
            AnnotatedBeanMethodActivator h = type.getAnnotation(AnnotatedBeanMethodActivator.class);
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


    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<AnnotatedParameterType> PARAMETER_ANNOTATION_CACHE = new ClassValue<>() {

        @Override
        protected AnnotatedParameterType computeValue(Class<?> type) {

            AnnotatedBeanVariableActivator h = type.getAnnotation(AnnotatedBeanVariableActivator.class);

            Class<? extends Annotation> cl = bindings.get(type.getName());
            if (cl != null) {
                Class<?> declaringClass = cl.getDeclaringClass();
                if (!Extension.class.isAssignableFrom(declaringClass)) {
                    throw new InternalExtensionException("oops");
                }
                @SuppressWarnings("unchecked")
                Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) declaringClass;
                return new AnnotatedParameterType(extensionClass);
            }

            if (h == null) {
                return null;
            }

            // checkExtensionClass(type, h.extension());
            return new AnnotatedParameterType(h.extension());
        }
    };

    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<ParameterType> PARAMETER_TYPE_CACHE = new ClassValue<>() {

        @Override
        protected ParameterType computeValue(Class<?> type) {
            BindingClassActivator h = type.getAnnotation(BindingClassActivator.class);
            Class<? extends Annotation> cl = bindings.get(type.getName());
            if (cl != null) {
                Class<?> declaringClass = cl.getDeclaringClass();
                if (!Extension.class.isAssignableFrom(declaringClass)) {
                    throw new InternalExtensionException("oops");
                }
                @SuppressWarnings("unchecked")
                Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) declaringClass;
                return new ParameterType(extensionClass);
            }

            if (h == null) {
                return null;
            }

            // checkExtensionClass(type, h.extension());
            return new ParameterType(h.extension());
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
            if (a.annotationType().isAnnotationPresent(BeanCustomActivator.class)) {
                holders.add(new AssemblyMetaHolder(a.annotationType()));
            }
        }
        Map<String, Class<? extends Annotation>> b = new HashMap<>();
        for (AssemblyMetaHolder h : holders) {
            for (String s : h.bindings) {
                b.put(s, h.annotationType);
            }
        }
        this.bindings = Map.copyOf(b);
    }

    @Nullable
    AnnotatedMethod testMethodAnnotation(Class<? extends Annotation> annotation) {
        return METHOD_ANNOTATION_CACHE.get(annotation);
    }

    @Nullable
    AnnotatedParameterType testParameterAnnotation(Class<? extends Annotation> annotation) {
        return PARAMETER_ANNOTATION_CACHE.get(annotation);
    }

    @Nullable
    ParameterType testParameterType(Class<?> parameterType) {
        return PARAMETER_TYPE_CACHE.get(parameterType);
    }

    static void checkExtensionClass(Class<?> annotationType, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, s -> new InternalExtensionException(s));
        if (extensionType.getModule() != annotationType.getModule()) {
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + extensionType + " must be declared in the same module");
        }
    }

    public static BeanHookModel of(Class<?> clazz) {
        return MODELS.get(clazz);
    }

    record AnnotatedMethod(Class<? extends Extension<?>> extensionType, boolean isInvokable) {}

    record AnnotatedParameterType(Class<? extends Extension<?>> extensionType) {}

    record ParameterType(Class<? extends Extension<?>> extensionType) {}
}
