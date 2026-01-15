/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.bean.sandbox.ForeignBeanTrigger;
import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import org.jspecify.annotations.Nullable;
import internal.app.packed.assembly.AssemblyMetaHolder;
import internal.app.packed.util.types.ClassUtil;

/**
 * Ideen er lidt at vi kan have foreign triggers
 */
public final class BeanTriggerModelCustom {

    private static final ClassValue<BeanTriggerModelCustom> MODELS = new ClassValue<>() {

        @Override
        protected BeanTriggerModelCustom computeValue(Class<?> type) {
            if (type == Object.class) {
                return new BeanTriggerModelCustom();
            }
            BeanTriggerModelCustom parent = BeanTriggerModelCustom.of(type.getSuperclass());
            Annotation[] meta = type.getDeclaredAnnotations();
            if (meta.length == 0) {
                return parent;
            }

            return new BeanTriggerModelCustom(parent, meta);
        }
    };

    final Map<String, Class<? extends Annotation>> bindings;

//    /** A cache of any extensions a particular annotation activates. */
//    private final ClassValue<ParameterAnnotatedCache> PARAMETER_ANNOTATION_CACHE = new ClassValue<>() {
//
//        @Override
//        protected ParameterAnnotatedCache computeValue(Class<?> type) {
//
//            OnAnnotatedVariable h = type.getAnnotation(OnAnnotatedVariable.class);
//
//            Class<? extends Annotation> cl = bindings.get(type.getName());
//            if (cl != null) {
//                Class<?> declaringClass = cl.getDeclaringClass();
//                if (!Extension.class.isAssignableFrom(declaringClass)) {
//                    throw new InternalExtensionException("oops");
//                }
//                @SuppressWarnings("unchecked")
//                Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) declaringClass;
//                return new ParameterAnnotatedCache(extensionClass);
//            }
//
//            if (h == null) {
//                return null;
//            }
//
//            // checkExtensionClass(type, h.extension());
//            return new ParameterAnnotatedCache(h.extension());
//        }
//    };
//
//    /** A cache of any extensions a particular annotation activates. */
//    private final ClassValue<ParameterTypeCache> PARAMETER_TYPE_CACHE = new ClassValue<>() {
//
//        @Override
//        protected ParameterTypeCache computeValue(Class<?> type) {
//            OnExtensionServiceBeanTrigger h = type.getAnnotation(OnExtensionServiceBeanTrigger.class);
//            OnExtensionServiceInteritedBeanTrigger ih = type.getAnnotation(OnExtensionServiceInteritedBeanTrigger.class);
//
//            if (h == null && ih == null) {
//                return null;
//            }
//            if (h != null && ih != null) {
//                throw new InternalExtensionException(
//                        "Cannot use both " + OnExtensionServiceBeanTrigger.class + " and " + OnExtensionServiceInteritedBeanTrigger.class + " on @" + type);
//            }
//
//            if (h != null) {
//
//            }
//
//            if (ih != null && type.isInterface()) {
//                throw new InternalExtensionException("@" + OnExtensionServiceInteritedBeanTrigger.class.getSimpleName()
//                        + " cannot be used on interfaces as interface annotations are never inherited, class = " + type);
//            }
//
//            // Customer class name bindings
//            Class<? extends Annotation> cl = bindings.get(type.getName());
//            if (cl != null) {
//                Class<?> declaringClass = cl.getDeclaringClass();
//                if (!Extension.class.isAssignableFrom(declaringClass)) {
//                    throw new InternalExtensionException("oops");
//                }
//                @SuppressWarnings("unchecked")
//                Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) declaringClass;
//                return new ParameterTypeCache(extensionClass, null);
//            }
//
//            Class<?> inherited = null;
//            if (ih != null) {
//                Class<?> clazz = type;
//                while (clazz != null) {
//                    if (clazz.getDeclaredAnnotation(OnExtensionServiceInteritedBeanTrigger.class) != null) {
//                        break;
//                    }
//                    clazz = clazz.getSuperclass();
//                }
//                inherited = clazz;
//            }
//            Class<? extends Extension<?>> clz = h == null ? ih.extension() : h.extension();
//            // checkExtensionClass(type, h.extension());
//            return new ParameterTypeCache(clz, inherited);
//        }
//    };

    // Tror ikke vi skal bruge den til noget
    @Nullable
    private final BeanTriggerModelCustom parent;

    private BeanTriggerModelCustom() {
        this.parent = null;
        this.bindings = Map.of();
    }

    private BeanTriggerModelCustom(BeanTriggerModelCustom parent, Annotation[] annotations) {
        this.parent = requireNonNull(parent);
        List<AssemblyMetaHolder> holders = new ArrayList<>();
        for (Annotation a : annotations) {
            if (a.annotationType().isAnnotationPresent(ForeignBeanTrigger.class)) {
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

    static void checkExtensionClass(Class<?> annotationType, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, s -> new InternalExtensionException(s));
        if (extensionType.getModule() != annotationType.getModule()) {
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + extensionType + " must be declared in the same module");
        }
    }

    public static BeanTriggerModelCustom of(Class<?> clazz) {
        return MODELS.get(clazz);
    }

}
