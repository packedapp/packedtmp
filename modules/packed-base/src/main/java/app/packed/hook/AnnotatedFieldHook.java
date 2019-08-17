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
package app.packed.hook;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;

import app.packed.util.FieldDescriptor;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.InvalidDeclarationException;

/**
 * A hook representing a field annotated with a specific type.
 * 
 * <p>
 * AnnotatedFieldHook are not safe to use by multiple threads
 **/
public interface AnnotatedFieldHook<T extends Annotation> {

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

    // Well it also works for instances
    <E> DelayedHookOperator<E> applyDelayed(FieldOperator<E> operator);

    /**
     * Applies the specified field operator to the underlying static field.
     * 
     * @param <E>
     *            the type of result from applying the operator
     * @param operator
     *            the operator to apply
     * @return the result from applying the operator to the static field
     * @throws UnsupportedOperationException
     *             if the underlying field is not a static field
     */
    <E> E applyOnStaticField(FieldOperator<E> operator);

    // Ellers ogsaa checker vi dette naar vi laver en en Supplier eller lignende...
    // Move these to descriptor????
    // hook.field().checkFinal().checkAssignableTo()....
    //// Nah... Tror gerne vi vil have annoteringen med...
    //// Det kan vi ikke faa hvis vi har den paa descriptoren...
    AnnotatedFieldHook<T> checkAssignableTo(Class<?> type);

    // Move checks to field operator????
    AnnotatedFieldHook<T> checkExactType(Class<?> type);

    /**
     * Checks that the underlying field is final.
     * 
     * @throws InvalidDeclarationException
     *             if the underlying field is not final
     * @return this hook
     * @see Modifier#isFinal(int)
     */
    AnnotatedFieldHook<T> checkFinal();

    /**
     * Checks that the underlying field is not final.
     * 
     * @throws InvalidDeclarationException
     *             if the underlying field is final
     * @return this hook
     * @see Modifier#isFinal(int)
     */
    AnnotatedFieldHook<T> checkNotFinal();

    /**
     * Checks that the underlying field is not static.
     * 
     * @throws InvalidDeclarationException
     *             if the underlying field is static
     * @return this hook
     * @see Modifier#isStatic(int)
     */
    AnnotatedFieldHook<T> checkNotStatic();

    /**
     * Checks that the underlying field is static.
     * 
     * @throws InvalidDeclarationException
     *             if the underlying field is not static
     * @return this hook
     * @see Modifier#isStatic(int)
     */
    AnnotatedFieldHook<T> checkStatic();

    /**
     * Returns a field descriptor for the underlying field.
     * 
     * @return a field descriptor for the underlying field
     */
    FieldDescriptor field();

    /**
     * Returns a method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a method handle for the underlying field with read access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created
     * @see Lookup#unreflectGetter(java.lang.reflect.Field)
     */
    MethodHandle getter();

    Lookup lookup(); // TODO remove this method

    /**
     * Returns a method handle giving read access to the underlying field.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying field is an instance field.
     * 
     * @return a method handle for the underlying field with write access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created, for example, if the underlying field is final
     * @see Lookup#unreflectSetter(java.lang.reflect.Field)
     */
    MethodHandle setter();

    /**
     * Returns a {@link VarHandle} for the underlying field.
     * 
     * @return a VarHandle for the underlying field
     * @throws IllegalAccessRuntimeException
     *             if the handle could not be created
     * @see Lookup#unreflectVarHandle(java.lang.reflect.Field)
     */
    VarHandle varHandle();
}
