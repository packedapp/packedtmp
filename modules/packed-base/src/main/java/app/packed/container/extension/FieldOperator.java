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
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.container.extension.hook.PackedFieldOperation;

/**
 *
 * <p>
 * This interface is not meant to be
 */
// An operator that when applied ....
public interface FieldOperator<T> {

    T apply(MethodHandles.Lookup lookup, Field field, Object instance);

    /**
     * @param lookup
     *            a lookup object that must have access to the specified field
     * @param field
     *            the field to operate on
     * @return the ope
     * @throws IllegalArgumentException
     *             if the specified field is not static
     */
    T applyStatic(MethodHandles.Lookup lookup, Field field);

    static FieldOperator<Consumer<Object>> consumer() {
        throw new UnsupportedOperationException();
    }

    static <E> FieldOperator<Consumer<E>> consumer(Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }
    //
    // static FieldOperator<Object> getOnce() {
    // // throw new XXX("Fields annotated with XXX cannot return null");
    // // Hmm. Saa burde vi jo ogsaa have suppliers der ikke returnere null....
    // throw new UnsupportedOperationException();
    // }

    /**
     * Returns a field operator that reads a field once.
     * 
     * @return a field operator that reads a field once
     */
    @Nullable
    static FieldOperator<Object> getOnce() {
        return new PackedFieldOperation.GetOnceInternalFieldOperation<>();
    }

    /**
     * Returns a field operator that reads a field once.
     * 
     * @param <E>
     *            the type of field (value to get)
     * @param fieldType
     *            the type of field
     * @return the new field operator
     */
    @SuppressWarnings("unchecked")
    @Nullable
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
    @Nullable
    static <E> FieldOperator<E> getOnce(TypeLiteral<E> fieldType) {
        return (FieldOperator<E>) getOnce(fieldType.rawType());
    }

    static FieldOperator<MethodHandle> getter() {
        // Giver mening at kalde denne getter hvis AnnotatedFieldHook ogsaa skal hedde Getter
        throw new UnsupportedOperationException();
    }

    static FieldOperator<MethodHandle> setter() {
        throw new UnsupportedOperationException();
    }
    // getAndSetter... is that atomic??????

    /**
     * Returns a field operator that creates a getter (Supplier).
     * 
     * @return a field operator that creates a getter.
     */
    static FieldOperator<Supplier<Object>> supplier() {
        return new PackedFieldOperation.SupplierInternalFieldOperation<>();
    }

    static <E> FieldOperator<Supplier<E>> supplier(Class<E> fieldType) {
        return new PackedFieldOperation.SupplierInternalFieldOperation<>();
    }

    static <E> FieldOperator<Supplier<E>> supplier(TypeLiteral<E> fieldType) {
        return new PackedFieldOperation.SupplierInternalFieldOperation<>();
    }
}
