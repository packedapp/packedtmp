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
package internal.app.packed.operation;

import java.lang.reflect.Type;

/**
 *
 */
// 2 Primære usecases
// Gem et generics ObjectStore
// Embed i en anden operation
// casted Result
//// Det er her jeg har misforstaaet det i lang tid. Det er ikke kun bean storage

// Default MT er return rawType();

public interface OperationReturnTemplate {

    /**
     * Checks that that the operation (typically method) returns void.
     * Or maybe it is a check, we do not adaption
     */
    default OperationReturnTemplate returnVoid(boolean adapt) {
        return returnClass(void.class);
    }

    // checks, but does not adapt
    OperationReturnTemplate withReturn(Type... type);
    OperationReturnTemplate withReturnAndAdapt(Type... type);
    OperationReturnTemplate withReturnAndAdapt(Class<?> adaptTo, Type... anyOf);

    OperationReturnTemplate withReturnKey();
    OperationReturnTemplate withReturnKey(Class<?> adaptedTo);

    OperationReturnTemplate withReturnVoid();
    OperationReturnTemplate withReturnVoidIgnoreActual();



    // Will ignore any return type

    // Returns an object. Will fail if void method // Methods annotated with @Provide/@Get cannot have void return type
    default OperationReturnTemplate withReturnObject() {
        return returnClass(Object.class);
    }

    OperationReturnTemplate returnClass(Class<?> clazz);

    // Ignore any return value
    // MethodType return type: X->Void
    OperationReturnTemplate returnIgnore();

    // Each record can have 0 or 1 parameters (Only 1 with 0 thorugh)
    // Will checkReturnAnyOf.
    // Will MT OneType->SealedType

    // It can A be a sealed interface with records, b be a record () which you can then add methods to
    /**
     * The specified class can be either
     *
     * A sealed interface where all permitted classes are records with a zero or a single parameter. 0 parameters indicates
     * that void may be returned. 1 parameters indicates one valid return value.
     *
     * B: a record class with one parameter
     *
     * @param sealedRecords
     * @return
     */
    OperationReturnTemplate returnWrapper(Class<?> sealedRecords);

    // Checks if the return value can be assigned to any of the components.
    // Checks assignment compatible with any of the record components.
    // But does not modify the return type

}
