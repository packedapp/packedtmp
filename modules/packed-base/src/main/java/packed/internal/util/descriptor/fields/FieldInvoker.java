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
package packed.internal.util.descriptor.fields;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;

import app.packed.inject.IllegalAccessRuntimeException;
import app.packed.util.FieldDescriptor;
import app.packed.util.InvalidDeclarationException;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/** A field invoker extends a field descriptor with functionality for getting and setting the value of the field. */
public abstract class FieldInvoker {

    /** The fields descriptor. */
    private final InternalFieldDescriptor descriptor;

    /** The var handle of the field. */
    private final VarHandle varHandle;

    /**
     * Creates a new field invoker.
     * 
     * @param descriptor
     *            the field descriptor
     * @param lookup
     *            the lookup object to use for access
     */
    public FieldInvoker(InternalFieldDescriptor descriptor, Lookup lookup) {
        this.descriptor = descriptor;
        try {
            this.varHandle = descriptor.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Field " + descriptor + " is not accessible for lookup object " + lookup, e);
        }
    }

    /**
     * Returns the descriptor of the field.
     * 
     * @return the descriptor of the field
     */
    public final FieldDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Sets the value of the field
     * 
     * @param instance
     *            the instance for which to set the value
     * @param value
     *            the value to set
     * @see VarHandle#set(Object...)
     */
    protected final void setField(Object instance, Object value) {
        varHandle.set(instance, value);
    }

    protected static String fieldCannotHaveBothAnnotations(InternalFieldDescriptor field, Class<? extends Annotation> annotationType1,
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
    protected static String fieldWithAnnotationCannotBeFinal(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        return "Cannot use @" + annotationType.getSimpleName() + " on final field: " + field + ", to resolve remove @" + annotationType.getSimpleName()
                + " or make the field non-final";
    }

    protected static void checkAnnotatedFieldIsNotStatic(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        if ((Modifier.isStatic(field.getModifiers()))) {
            throw new InvalidDeclarationException("Cannot use @" + annotationType.getSimpleName() + " on static field: " + field + ", to resolve remove @"
                    + annotationType.getSimpleName() + " or make the field non-static");
        }
    }

    protected static void checkAnnotatedFieldIsNotFinal(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        if ((Modifier.isStatic(field.getModifiers()))) {
            throw new InvalidDeclarationException("Fields annotated with @" + annotationType.getSimpleName() + " must be final, field = " + field
                    + ", to resolve remove @" + annotationType.getSimpleName() + " or make the field final");
        }
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
    protected static String fieldWithAnnotationCannotBeStatic(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
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
    protected static String fieldWithAnnotationMustBeFinal(InternalFieldDescriptor field, Class<? extends Annotation> annotationType) {
        return "Fields annotated with @" + annotationType.getSimpleName() + " must be final, field = " + field + ", to resolve remove @"
                + annotationType.getSimpleName() + " or make the field final";
    }
}
