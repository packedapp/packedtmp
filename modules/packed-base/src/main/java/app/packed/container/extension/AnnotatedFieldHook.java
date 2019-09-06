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
package app.packed.container.extension;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.reflect.FieldDescriptor;
import app.packed.reflect.UncheckedIllegalAccessException;

import java.lang.invoke.VarHandle;

/**
 * A hook representing a field annotated with a specific annotation.
 * 
 * <p>
 * AnnotatedFieldHook are never safe to use by multiple threads. Furthermore something about some methods only being
 * active when building XX
 * 
 * @param <T>
 *            the type of annotation this hook matches
 **/
public interface AnnotatedFieldHook<T extends Annotation> {

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

    <E> HookApplicator<E> applicator(FieldOperator<E> operator);

    /**
     * Applies the specified operator to the underlying field.
     * 
     * @param <E>
     *            the type of result from applying the operator
     * @param operator
     *            the operator to apply
     * @return the result from applying the operator to the static field
     * @throws UnsupportedOperationException
     *             if the underlying field is not a static field. Or if the underlying field is final, and the operator
     *             needs write access
     * @throws UncheckedIllegalAccessException
     *             if access checking failed when accessing the field
     */
    <E> E applyStatic(FieldOperator<E> operator);

    /**
     * Returns a descriptor for the underlying field.
     * 
     * @return a descriptor for the underlying field
     */
    FieldDescriptor field();

    /**
     * Returns a method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a method handle for the underlying field with read access
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflectGetter(java.lang.reflect.Field)
     */
    MethodHandle getter();

    /**
     * Returns a method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a method handle for the underlying field with write access
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
     * @throws UnsupportedOperationException
     *             if the field is final
     * @see Lookup#unreflectSetter(java.lang.reflect.Field)
     */
    MethodHandle setter();

    /**
     * Returns a {@link VarHandle} for the underlying field.
     * 
     * @return a VarHandle for the underlying field
     * @throws UncheckedIllegalAccessException
     *             if access checking fails
     * @see Lookup#unreflectVarHandle(java.lang.reflect.Field)
     */
    VarHandle varHandle();
}
