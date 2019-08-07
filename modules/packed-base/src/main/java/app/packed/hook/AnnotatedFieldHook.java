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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.util.FieldDescriptor;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.TypeLiteral;

/** A hook representing a field annotated with a specific type. */
public interface AnnotatedFieldHook<T extends Annotation> {

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

    // boolean isStatic() <---- Saa meget lettere hvis feltet er statisk....

    // Ellers ogsaa checker vi dette naar vi laver en en Supplier eller lignende...
    default AnnotatedFieldHook<T> checkAssignableTo(Class<?> type) {
        throw new UnsupportedOperationException();
    }

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
     * Returns the annotated field.
     * 
     * @return the annotated field
     */
    FieldDescriptor field();

    Lookup lookup(); // TODO remove this method

    // Drop TypeLiteral taenker jeg...
    <A> A newAccessor(ComponentConfiguration cc, Class<A> accessorType);

    <A> A newAccessor(ComponentConfiguration cc, Class<A> accessorType, VarHandle.AccessMode accessMode);

    <A> A newAccessor(ComponentConfiguration cc, TypeLiteral<A> accessorType);

    <A> A newAccessor(ComponentConfiguration cc, TypeLiteral<A> accessorType, VarHandle.AccessMode accessMode);

    BiPredicate<?, ?> newCompareAndSetAccessor(ComponentConfiguration cc);

    <E> BiPredicate<E, E> newCompareAndSetAccessor(ComponentConfiguration cc, Class<E> fieldType);

    <E> BiPredicate<E, E> newCompareAndSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType);

    // Checks that type matches...
    // And that AFH.bundle == ComponentConfiguration.bundle()
    // Return with a fixed if static.
    // Else we register a callback, that sets the instance. Throwing ISE
    // until the instance has been set
    Supplier<?> newGetAccessor(ComponentConfiguration cc);

    <E> Supplier<E> newGetAccessor(ComponentConfiguration cc, Class<E> fieldType);

    <E> Supplier<E> newGetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType);

    Function<?, ?> newGetAndSetAccessor(ComponentConfiguration cc);

    <E> Function<? super E, E> newGetAndSetAccessor(ComponentConfiguration cc, Class<E> fieldType);

    <E> Function<? super E, E> newGetAndSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType);

    /**
     * Creates a method handle giving read access to the underlying field. If the underlying field is an instance field. The
     * component instance must be explicitly provided by users of this method.
     * 
     * @return a new method handle with read access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created
     * @see Lookup#unreflectGetter(java.lang.reflect.Field)
     */
    MethodHandle newMethodHandleGetter();

    /**
     * Creates a method handle giving read access to the underlying field. If the underlying field is an instance field. The
     * component instance must be explicitly provided by users of this method.
     * 
     * @return a new method handle with read access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created, for example, if the underlying field is final
     * @see Lookup#unreflectSetter(java.lang.reflect.Field)
     */
    MethodHandle newMethodHandleSetter();

    // ????
    Consumer<?> newSetAccessor(ComponentConfiguration cc);

    // Are we going to perform type checking? other that what VarHandle does????
    <E> Consumer<? super E> newSetAccessor(ComponentConfiguration cc, Class<E> fieldType);

    <E> Consumer<? super E> newSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType);

    /**
     * Creates a new {@link VarHandle} for the underlying field. If the underlying field is an instance field. The component
     * instance must be explicitly provided by the user.
     * 
     * @return a new VarHandle for the underlying field
     * @throws IllegalAccessRuntimeException
     *             if a var handle could not be created
     * @see Lookup#unreflectVarHandle(java.lang.reflect.Field)
     */
    VarHandle newVarHandle();

    // Ideen er egentlig at vi kompilere
    // compile() <- maybe compile, maybe only
    AnnotatedFieldHook<T> optimize();

    default AnnotatedFieldHook<T> runOnReady(Runnable r) {
        throw new UnsupportedOperationException();
    }
}
