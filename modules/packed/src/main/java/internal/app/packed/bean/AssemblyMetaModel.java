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
import java.util.Map;

import app.packed.base.Nullable;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;

/**
 *
 */
public final class AssemblyMetaModel {

    @Nullable
    private final AssemblyMetaModel parent;

    private static final ClassValue<AssemblyMetaModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyMetaModel computeValue(Class<?> type) {
            if (type == Object.class) {
                return new AssemblyMetaModel();
            }
            AssemblyMetaModel parent = AssemblyMetaModel.of(type.getSuperclass());
            Meta[] meta = type.getAnnotationsByType(Meta.class);
            if (meta.length == 0) {
                return parent;
            }
            return new AssemblyMetaModel(parent, meta);
        }
    };

    private AssemblyMetaModel() {
        this.parent = null;
    }

    private AssemblyMetaModel(AssemblyMetaModel parent, Meta[] meta) {
        this.parent = requireNonNull(parent);
    }

    public static AssemblyMetaModel of(Class<?> clazz) {
        return MODELS.get(clazz);
    }

    @interface Meta {

    }

    FieldRecord lookupFieldAnnotation(Class<? extends Annotation> fieldAnnotation) {
        return FIELD_ANNOTATION_CACHE.get(fieldAnnotation);
    }

    final Map<String, String> fieldOrBindingHook = Map.of();

    record FieldRecord(Class<? extends Annotation> annotationType, Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable,
            boolean isProvision) {}

    /** A cache of any extensions a particular annotation activates. */
    private final ClassValue<FieldRecord> FIELD_ANNOTATION_CACHE = new ClassValue<>() {

        @Override
        protected FieldRecord computeValue(Class<?> type) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationType = (Class<? extends Annotation>) type;
            FieldHook fieldHook = type.getAnnotation(FieldHook.class);
            BindingHook provisionHook = type.getAnnotation(BindingHook.class);

            fieldOrBindingHook.get(type.getName());
            
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
}
