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
package packed.internal.componentcache;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.util.FieldDescriptor;
import app.packed.util.IllegalAccessRuntimeException;

/** The default implementation of {@link AnnotatedFieldHook}. */
final class PackedAnnotatedFieldHook<T extends Annotation> implements AnnotatedFieldHook<T> {

    /** The annotation value */
    private final T annotation;

    /** The annotated field. */
    private final Field field;

    /** A lookup object used to create various handlers. */
    private final Lookup lookup;

    PackedAnnotatedFieldHook(Lookup lookup, Field field, T annotation) {
        this.lookup = requireNonNull(lookup);
        this.field = requireNonNull(field);
        this.annotation = requireNonNull(annotation);
    }

    @Override
    public T annotation() {
        return annotation;
    }

    @Override
    public FieldDescriptor field() {
        return FieldDescriptor.of(field);
    }

    @Override
    public Lookup lookup() {
        return lookup;// Temporary method
    }

    @Override
    public MethodHandle newMethodHandleGetter() {
        field.setAccessible(true);
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a MethodHandle", e);
        }
    }

    @Override
    public MethodHandle newMethodHandleSetter() {
        field.setAccessible(true);
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a MethodHandle", e);
        }
    }

    @Override
    public VarHandle newVarHandle() {
        field.setAccessible(true);
        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Could not create a VarHandle", e);
        }
    }

}
