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

/** A hook representing a field annotated with a specific type. */
public interface AnnotatedFieldHook<T extends Annotation> {

    <E> RuntimeAccessor<E> accessAtRuntime(FieldOperator<E> operator);

    /**
     * 
     * @param <E>
     * @param accessor
     * @return stuff
     * @throws UnsupportedOperationException
     *             if the underlying field is not static
     * @throws IllegalStateException
     *             if invoked outside of building an aggregate
     */
    <E> E accessStatic(FieldOperator<E> accessor);

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

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

    // Drop TypeLiteral taenker jeg...

    /**
     * Returns the annotated field.
     * 
     * @return the annotated field
     */
    FieldDescriptor field();

    Lookup lookup(); // TODO remove this method

    /**
     * Creates a method handle giving read access to the underlying field. If the underlying field is an instance field. The
     * instance must be explicitly provided by users of this method.
     * 
     * @return a new method handle with read access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created
     * @see Lookup#unreflectGetter(java.lang.reflect.Field)
     */
    MethodHandle newGetter();

    /**
     * Creates a method handle giving read access to the underlying field. If the underlying field is an instance field. The
     * instance must be explicitly provided by users of this method.
     * 
     * @return a new method handle with read access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created, for example, if the underlying field is final
     * @see Lookup#unreflectSetter(java.lang.reflect.Field)
     */
    MethodHandle newSetter();

    /**
     * Creates a new {@link VarHandle} for the underlying field. If the underlying field is an instance field. The instance
     * must be explicitly provided by the user.
     * 
     * @return a new VarHandle for the underlying field
     * @throws IllegalAccessRuntimeException
     *             if a var handle could not be created
     * @see Lookup#unreflectVarHandle(java.lang.reflect.Field)
     */
    VarHandle newVarHandle();
}

// Check the type
// checkNotNull()???Nah that could get complicated.... Maybe at some point
// because should we check it every time we access the field, or only once
