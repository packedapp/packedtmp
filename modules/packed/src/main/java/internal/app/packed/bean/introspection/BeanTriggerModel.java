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
package internal.app.packed.bean.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import app.packed.context.Context;
import app.packed.extension.Extension;
import org.jspecify.annotations.Nullable;
import internal.app.packed.util.PackedAnnotationList;

/**
 *
 */
public interface BeanTriggerModel {

    @Nullable
    ParameterTypeCache testParameterType(Class<?> parameterType);

    @Nullable
    ParameterAnnotatedCache testParameterAnnotation(Class<? extends Annotation> annotation);

    @Nullable
    FieldCache testField(Class<? extends Annotation> annotationType);

    @Nullable
    OnAnnotatedMethodCache testMethod(Class<? extends Annotation> annotationType);

    record ParameterAnnotatedCache(BeanIntrospectorClassModel bim) {}

    record ParameterTypeCache(BeanIntrospectorClassModel bim, @Nullable Class<?> definingIfInherited, Set<Class<? extends Context<?>>> requiredContexts) {}

    sealed interface FieldCache permits OnAnnotatedVariableCache, OnAnnotatedFieldCache {
        void handleOne(BeanScanner scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations);

        BeanIntrospectorClassModel bim();
    }

    /**
     *
     * Represents an annotated annotated with {@link AnnotatedVariableHook}.
     */
    record OnAnnotatedVariableCache(BeanIntrospectorClassModel bim) implements FieldCache {
        public Class<? extends Extension<?>> extensionType() {
            return bim.extensionClass;
        }

        @Override
        public void handleOne(BeanScanner scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations) {
            IntrospectorOnVariable.process(scanner.introspector(bim()), field, annotations, triggeringAnnotations, this);
        }
    }

    record OnAnnotatedMethodCache(BeanIntrospectorClassModel bim, boolean isInvokable) {
        public Class<? extends Extension<?>> extensionType() {
            return bim.extensionClass;
        }
    }

    /**
     * A cache for {@link app.packed.bean.scanning.BeanTrigger.OnAnnotatedField}.
     */
    record OnAnnotatedFieldCache(BeanIntrospectorClassModel bim, boolean isGettable, boolean isSettable, Class<? extends Context<?>>[] requiresContext)
            implements FieldCache {
        public Class<? extends Extension<?>> extensionType() {
            return bim.extensionClass;
        }

        @Override
        public void handleOne(BeanScanner scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations) {
            IntrospectorOnField.process(scanner.introspector(bim()), field, annotations, triggeringAnnotations, this);
        }
    }
}
