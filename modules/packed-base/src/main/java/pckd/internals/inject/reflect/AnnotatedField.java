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
package pckd.internals.inject.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import app.packed.inject.NotAccessibleException;
import pckd.internals.util.descriptor.InternalFieldDescriptor;

/**
 *
 */
public class AnnotatedField {

    final InternalFieldDescriptor descriptor;

    final VarHandle handle;

    AnnotatedField(InternalFieldDescriptor descriptor, VarHandle handle) {
        this.descriptor = descriptor;
        this.handle = handle;
    }

    public InternalFieldDescriptor descriptor() {
        return descriptor;
    }

    public void setField(Object instance, Object value) {
        handle.set(instance, value);
    }

    static String fieldCannotHaveBothAnnotations(InternalFieldDescriptor field, Class<? extends Annotation> annotationType1,
            Class<? extends Annotation> annotationType2) {
        return "Cannot use both @" + annotationType1.getSimpleName() + " and @" + annotationType1.getSimpleName() + " on field: " + field
                + ", to resolve remove one of the annotations.";
    }

    /**
     * Creates an error message for using an annotation on a final field.
     *
     * @param field
     *            the field
     * @param annotationType
     *            the annotation
     * @return the error message
     */
    static String fieldWithAnnotationCannotBeFinal(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        return "Cannot use @" + annotationType.getSimpleName() + " on final field: " + field + ", to resolve remove @" + annotationType.getSimpleName()
                + " or make the field non-final";
    }

    /**
     * Creates an error message for using an annotation on a static field.
     *
     * @param field
     *            the field
     * @param annotationType
     *            the annotation
     * @return the error message
     */
    static String fieldWithAnnotationCannotBeStatic(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        return "Cannot use @" + annotationType.getSimpleName() + " on static field: " + field + ", to resolve remove @" + annotationType.getSimpleName()
                + " or make the field non-static";
    }

    /**
     * Creates an error message for using an annotation on a field that is not final.
     *
     * @param field
     *            the field
     * @param annotationType
     *            the annotation
     * @return the error message
     */
    static String fieldWithAnnotationMustBeFinal(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        return "Fields annotated with @" + annotationType.getSimpleName() + " must be final, field = " + field + ", to resolve remove @"
                + annotationType.getSimpleName() + " or make the field final";
    }

    static VarHandle unreflect(Lookup lookup, Field f) {
        try {
            return lookup.unreflectVarHandle(f);
        } catch (IllegalAccessException e) {
            throw new NotAccessibleException("Field is not accessible for lookup object " + lookup, e);
        }
    }
}
