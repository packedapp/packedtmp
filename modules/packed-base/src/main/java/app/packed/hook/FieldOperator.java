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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import app.packed.hook.field.InternalFieldOperation;
import app.packed.util.TypeLiteral;

/**
 *
 * <p>
 * This interface is not meant to be
 */
public interface FieldOperator<T> {

    T accessStatic(MethodHandles.Lookup lookup, Field field);

    static FieldOperator<Object> getOnce() {
        return new InternalFieldOperation.GetOnceInternalFieldOperation<>();
    }

    @SuppressWarnings("unchecked")
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
     * Returns a field mapper that creates a supplier
     * 
     * @return a field mapper that creates suppliers.
     */
    static FieldOperator<Supplier<Object>> supplier() {
        return new InternalFieldOperation.SupplierInternalFieldOperation<>();
    }
}
