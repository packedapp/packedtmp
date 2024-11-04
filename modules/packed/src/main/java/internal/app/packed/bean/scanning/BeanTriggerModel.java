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
import java.lang.reflect.Field;

import app.packed.context.Context;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
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

    record ParameterAnnotatedCache(BeanIntrospectorModel bim) {}

    record ParameterTypeCache(BeanIntrospectorModel bim, @Nullable Class<?> definingIfInherited) {}

    sealed interface FieldCache permits OnAnnotatedVariableCache, OnAnnotatedFieldCache {
        void handleOne(BeanScanner scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations);

        BeanIntrospectorModel bim();
    }

    /**
     *
     * Represents an annotated annotated with {@link AnnotatedVariableHook}.
     */
    record OnAnnotatedVariableCache(BeanIntrospectorModel bim) implements FieldCache {
        public Class<? extends Extension<?>> extensionType() {
            return bim.extensionClass;
        }

        @Override
        public void handleOne(BeanScanner scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations) {
            IntrospectorOnVariable.process(scanner.computeIntrospector(bim()), field, annotations, triggeringAnnotations, this);
        }
    }

    record OnAnnotatedMethodCache(BeanIntrospectorModel bim, boolean isInvokable) {
        public Class<? extends Extension<?>> extensionType() {
            return bim.extensionClass;
        }
    }

    /**
     * A cache for {@link app.packed.bean.scanning.BeanTrigger.OnAnnotatedField}.
     */
    record OnAnnotatedFieldCache(BeanIntrospectorModel bim, boolean isGettable, boolean isSettable, Class<? extends Context<?>>[] requiresContext)
            implements FieldCache {
        public Class<? extends Extension<?>> extensionType() {
            return bim.extensionClass;
        }

        @Override
        public void handleOne(BeanScanner scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations) {
            IntrospectorOnField.process(scanner.computeIntrospector(bim()), field, annotations, triggeringAnnotations, this);
        }
    }
}
