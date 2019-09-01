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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.util.TypeLiteral;
import packed.internal.container.extension.hook.PackedFieldOperator;

/**
 * <p>
 * Operators are typically created once and stored in a static field.
 * <p>
 * This interface is not meant to be
 */
// An operator that when applied ....
public interface FieldOperator<T> {

    /**
     * Returns a new operator that will fail to work with non final-fields.
     * 
     * @return the new operator
     * @see Modifier#isFinal(int)
     */
    FieldOperator<T> requireFinal();

    T apply(MethodHandles.Lookup caller, Field field, Object instance);

    /**
     * Applies this operator to the specified static field.
     * 
     * @param caller
     *            a lookup object that must have access to the specified field
     * @param field
     *            the field to operate on
     * @return the result of applying this operator
     * @throws IllegalArgumentException
     *             if the specified field is not static
     */
    T applyStatic(MethodHandles.Lookup caller, Field field);

    /**
     * @return stuff
     */
    static FieldOperator<Consumer<Object>> consumer() {
        throw new UnsupportedOperationException();
    }

    static <E> FieldOperator<Consumer<E>> consumer(Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a field operator that reads a field once, and returns the value (possible null).
     * 
     * @return a field operator that reads a field once
     */
    static FieldOperator<Object> getOnce() {
        return new PackedFieldOperator.GetOnceInternalFieldOperation<>();
    }

    /**
     * Returns a field operator that can be used to read a field exactly once.
     * 
     * @param <E>
     *            the type of field (value to get)
     * @param fieldType
     *            the type of field
     * @return the new field operator
     */
    @SuppressWarnings("unchecked")
    // TODO do we do exact check...
    // Yeah, or throws ClassCastException..
    // Den er en lille smule ligegyldig here.
    // Da brugeren formentlig vil lave et cast lige bagefter
    static <E> FieldOperator<E> getOnce(Class<E> fieldType) {
        return (FieldOperator<E>) getOnce();
    }

    /**
     * <p>
     * So this is basically just syntantic sugar.
     * 
     * @param <E>
     * @param fieldType
     * @return stuff
     */
    @SuppressWarnings("unchecked")
    // We could theoretically check the signature of the field....
    static <E> FieldOperator<E> getOnce(TypeLiteral<E> fieldType) {
        return (FieldOperator<E>) getOnce(fieldType.rawType());
    }

    /**
     * Returns a field operator that creates {@link MethodHandle} with getter semantics as outlined in
     * {@link Lookup#unreflectGetter(Field)}.
     * 
     * @return a field operator that will create a getter
     * @see Lookup#unreflectGetter(Field)
     */
    static FieldOperator<MethodHandle> getter() {
        // Giver mening at kalde denne getter hvis AnnotatedFieldHook ogsaa skal hedde Getter
        throw new UnsupportedOperationException();
    }

    static FieldOperator<MethodHandle> setter() {
        throw new UnsupportedOperationException();
    }
    // getAndSetter... is that atomic??????

    /**
     * Returns a field operator that creates a Supplier getter (Supplier).
     * 
     * @return a field operator that creates a getter.
     */
    static FieldOperator<Supplier<Object>> supplier() {
        return new PackedFieldOperator.SupplierInternalFieldOperation<>();
    }

    static <E> FieldOperator<Supplier<E>> supplier(Class<E> fieldType) {
        return new PackedFieldOperator.SupplierInternalFieldOperation<>();
    }

    static <E> FieldOperator<Supplier<E>> supplier(TypeLiteral<E> fieldType) {
        return new PackedFieldOperator.SupplierInternalFieldOperation<>();
    }
}

//// Ellers ogsaa checker vi dette naar vi laver en en Supplier eller lignende...
//// Move these to descriptor????
//// hook.field().checkFinal().checkAssignableTo()....
////// Nah... Tror gerne vi vil have annoteringen med...
////// Det kan vi ikke faa hvis vi har den paa descriptoren...
// AnnotatedFieldHook<T> checkAssignableTo(Class<?> type);
//
//// Move checks to field operator????
//// FieldOperator.checkFinalField().checkStatic
// AnnotatedFieldHook<T> checkExactType(Class<?> type);
//
/// **
// * Checks that the underlying field is final.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is not final
// * @return this hook
// * @see Modifier#isFinal(int)
// */
// AnnotatedFieldHook<T> checkFinal();
//
/// **
// * Checks that the underlying field is not final.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is final
// * @return this hook
// * @see Modifier#isFinal(int)
// */
// AnnotatedFieldHook<T> checkNotFinal();
//
/// **
// * Checks that the underlying field is not static.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is static
// * @return this hook
// * @see Modifier#isStatic(int)
// */
// AnnotatedFieldHook<T> checkNotStatic();
//
/// **
// * Checks that the underlying field is static.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is not static
// * @return this hook
// * @see Modifier#isStatic(int)
// */
// AnnotatedFieldHook<T> checkStatic();
