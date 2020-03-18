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
package app.packed.base.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import app.packed.base.reflect.FieldDescriptor;

/**
 * A wrapper for a {@link Field} and a {@link Lookup} object.
 * 
 * @apiNote In the future, if the Java language permits, {@link AccessibleField} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface AccessibleField {

    /**
     * Returns a descriptor for the underlying field.
     * 
     * @return a descriptor for the underlying field
     */
    FieldDescriptor descriptor();

    /**
     * Returns a new method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a new method handle for the underlying field with read access
     * @throws IllegalStateException
     *             if trying to invoke this method from outside of the intended working scope
     * @see Lookup#unreflectGetter(java.lang.reflect.Field)
     */
    MethodHandle newGetter();

    /**
     * Returns a new method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a new method handle for the underlying field with write access
     * @throws UnsupportedOperationException
     *             if the underlying field is final
     * @throws IllegalStateException
     *             if trying to invoke this method after the hook has been constructed
     * @see Lookup#unreflectSetter(java.lang.reflect.Field)
     */
    MethodHandle newSetter();

    /**
     * Returns a new {@link VarHandle} for the underlying field.
     * 
     * @return a new VarHandle for the underlying field
     * @throws IllegalStateException
     *             if trying to invoke this method after the hook has been constructed
     * @see Lookup#unreflectVarHandle(java.lang.reflect.Field)
     */
    VarHandle newVarHandle();

    static boolean isAccessibleForGetter(MethodHandles.Lookup lookup, Field field) {
        throw new UnsupportedOperationException();
    }

    static boolean isAccessibleForGetter(MethodHandles.Lookup lookup, FieldDescriptor field) {
        throw new UnsupportedOperationException();
    }
}
// I think we only construct it if it is open...
//* @throws UncheckedIllegalAccessException
//*             if access checking fails
