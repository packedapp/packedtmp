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
package app.packed.util;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import packed.internal.util.descriptor.InternalFieldDescriptor;

/**
 * A field descriptor.
 * <p>
 * Unlike the {@link Field} class, this interface contains no mutable operations, so it can be freely shared.
 */
public interface FieldDescriptor extends VariableDescriptor, Member {

    /**
     * Returns whether or not this field is a static field.
     *
     * @return whether or not this field is a static field
     * @see Modifier#isStatic(int)
     */
    default boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    /**
     * Creates a new {@link Field} corresponding to this descriptor.
     *
     * @return a new field
     */
    Field newField();

    /**
     * Creates a new descriptor by finding a field with the specified declaring class and name.
     *
     * @param type
     *            the type that declares the field
     * @param fieldName
     *            the name of the field
     * @return a new field descriptor
     * @throws IllegalArgumentException
     *             if a field with the specified name is not declared by the specified type
     * @see Class#getDeclaredField(String)
     */
    static FieldDescriptor of(Class<?> type, String fieldName) {
        return InternalFieldDescriptor.of(type, fieldName);
    }

    /**
     * Returns a descriptor from the specified field.
     *
     * @param field
     *            the field to return a descriptor for
     * @return a descriptor from the specified field
     */
    static FieldDescriptor of(Field field) {
        return of(field.getDeclaringClass(), field.getName());
    }

    /**
     * Unreflects this field.
     * 
     * @param lookup
     *            the lookup object to use for unreflecting this field
     * @return a VarHandle corresponding to this field
     * @throws IllegalAccessException
     *             if the lookup object does not have access to the field
     * @see Lookup#unreflectVarHandle(Field)
     */
    // TODO not sure I want this on an interface?????? Maybe put back on internal class again...
    VarHandle unreflect(Lookup lookup) throws IllegalAccessException;
}
