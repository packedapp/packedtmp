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
package packed.util.invokers;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import packed.util.descriptor.InternalFieldDescriptor;

/**
 * A field that can be read and written
 */
public class FieldInvoker {
    private final InternalFieldDescriptor descriptor;

    private final Field field;

    FieldInvoker(Field field) {
        descriptor = InternalFieldDescriptor.of(field);
        this.field = field;
    }

    public InternalFieldDescriptor descriptor() {
        return descriptor;
    }

    VarHandle varHandle;

    /**
     * Sets the field on the specified object to the specified value.
     *
     * @param instance
     *            the instance for whose field to set
     * @param value
     *            the value to set
     * @throws IllegalAccessException
     *             if the field could not be set
     */
    public void set(Object instance, Object value) throws IllegalAccessException {
        if (!Modifier.isPublic(field.getModifiers()) && !field.canAccess(instance)) {
            field.trySetAccessible();
        }
        field.set(instance, value);
    }

    public static FieldInvoker of(Field field) {
        return new FieldInvoker(field);
    }
}
