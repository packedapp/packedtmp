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

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.bean.BeanTrigger.AutoInjectInheritable;
import app.packed.bean.BeanTrigger.OnAnnotatedField;
import app.packed.bean.BeanTrigger.OnAnnotatedMethod;
import app.packed.bean.BeanTrigger.OnAnnotatedVariable;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;

/**
 *
 */
public final class BeanTriggerModelDefaults implements BeanTriggerModel {

    /** A cache of field annotations. */
    private static final ClassValue<OnAnnotatedFieldCache> ANNOTATED_FIELD_HOOK_CACHE = new ClassValue<>() {

        @Override
        protected OnAnnotatedFieldCache computeValue(Class<?> type) {
            OnAnnotatedField annotation = type.getAnnotation(OnAnnotatedField.class);
            if (annotation == null) {
                return null;
            }

            if (type.isAssignableFrom(OnAnnotatedVariable.class)) {
                throw new InternalExtensionException(type + " cannot both be annotated with " + OnAnnotatedField.class + " and " + OnAnnotatedVariable.class);
            }

            BeanIntrospectorClassModel bim = bim(type, annotation.introspector());
            return new OnAnnotatedFieldCache(bim, annotation.allowGet(), annotation.allowSet(), annotation.requiresContext());
        }
    };

    /** A cache of field annotations. */
    private static final ClassValue<OnAnnotatedVariableCache> ANNOTATED_VARIABLE_HOOK_CACHE = new ClassValue<>() {

        @Override
        protected OnAnnotatedVariableCache computeValue(Class<?> type) {
            OnAnnotatedVariable annotation = type.getAnnotation(OnAnnotatedVariable.class);
            if (annotation == null) {
                return null; // Annotation not annotated with AnnotatedVariableHook
            }

            BeanIntrospectorClassModel bim = bim(type, annotation.introspector());
            return new OnAnnotatedVariableCache(bim);
        }
    };

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<OnAnnotatedMethodCache> METHOD_ANNOTATION_CACHE = new ClassValue<>() {

        @Override
        protected OnAnnotatedMethodCache computeValue(Class<?> type) {
            OnAnnotatedMethod annotation = type.getAnnotation(OnAnnotatedMethod.class);
            if (annotation == null) {
                return null;
            }

            BeanIntrospectorClassModel bim = bim(type, annotation.introspector());
            return new OnAnnotatedMethodCache(bim, annotation.allowInvoke());
        }
    };

    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<ParameterAnnotatedCache> PARAMETER_ANNOTATION_CACHE = new ClassValue<>() {

        @Override
        protected ParameterAnnotatedCache computeValue(Class<?> type) {
            OnAnnotatedVariable annotation = type.getAnnotation(OnAnnotatedVariable.class);
            if (annotation == null) {
                return null;
            }
            BeanIntrospectorClassModel bim = bim(type, annotation.introspector());
            return new ParameterAnnotatedCache(bim);
        }
    };

    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<ParameterTypeCache> PARAMETER_TYPE_CACHE = new ClassValue<>() {

        @Override
        protected ParameterTypeCache computeValue(Class<?> type) {
            AutoInject h = type.getAnnotation(AutoInject.class);
            AutoInjectInheritable ih = type.getAnnotation(AutoInjectInheritable.class);

            if (h == null && ih == null) {
                return null;
            }

            if (h != null && ih != null) {
                throw new InternalExtensionException(
                        "Cannot use both " + AutoInject.class + " and " + AutoInjectInheritable.class + " on @" + type);
            }

            if (h != null) {
                BeanIntrospectorClassModel bim = bim(type, h.introspector());
                return new ParameterTypeCache(bim, null);
            } else {
//                throw new InternalExtensionException("@" + OnExtensionServiceInteritedBeanTrigger.class.getSimpleName()
//                        + " cannot be used on interfaces as interface annotations are never inherited, class = " + type);
                BeanIntrospectorClassModel bim = bim(type, ih.introspector());

                Class<?> inherited = type;
                while (inherited != null) {
                    if (inherited.getDeclaredAnnotation(AutoInjectInheritable.class) != null) {
                        break;
                    }
                    inherited = inherited.getSuperclass();
                }

                return new ParameterTypeCache(bim, inherited);
            }
        }
    };

    /** {@inheritDoc} */
    @Override
    public @Nullable FieldCache testField(Class<? extends Annotation> annotationType) {
        FieldCache fc = ANNOTATED_FIELD_HOOK_CACHE.get(annotationType);
        if (fc == null) {
            fc = ANNOTATED_VARIABLE_HOOK_CACHE.get(annotationType);
        }
        return fc;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable OnAnnotatedMethodCache testMethod(Class<? extends Annotation> annotationType) {
        return METHOD_ANNOTATION_CACHE.get(annotationType);
    }

    @Override
    @Nullable
    public ParameterAnnotatedCache testParameterAnnotation(Class<? extends Annotation> annotation) {
        return PARAMETER_ANNOTATION_CACHE.get(annotation);
    }

    @Override
    @Nullable
    public ParameterTypeCache testParameterType(Class<?> parameterType) {
        if (parameterType.getModule() == BeanScanner.JAVA_BASE_MODULE) {
            return null;
        }
        return PARAMETER_TYPE_CACHE.get(parameterType);
    }

    private static BeanIntrospectorClassModel bim(Class<?> annotationType, Class<? extends BeanIntrospector<?>> beanIntrospectorType) {
        BeanIntrospectorClassModel bim = BeanIntrospectorClassModel.of(beanIntrospectorType);
        if (bim.extensionClass.getModule() != annotationType.getModule()) {
            // IO.println(bim.extensionClass.getModule());
            // IO.println(annotationType.getModule());
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + bim.extensionClass + " must be declared in the same module");
        }
        return bim;
    }

    @Nullable
    private static OnAnnotatedFieldCache findOnAnnotatedField(Class<? extends Annotation> annotationType) {
        return ANNOTATED_FIELD_HOOK_CACHE.get(annotationType);
    }

    @Nullable
    static OnAnnotatedVariableCache findOnAnnotatedVariable(Class<? extends Annotation> annotationType) {
        return ANNOTATED_VARIABLE_HOOK_CACHE.get(annotationType);
    }
}
