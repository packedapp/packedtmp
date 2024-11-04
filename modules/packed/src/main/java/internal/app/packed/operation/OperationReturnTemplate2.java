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

// Solutions
// We need dynamic types to be able to support embedded operations, no way around it.
// I think this means we cannot know the invocation type before hand.

// I don't think the template should know anything about Type(s)

public interface OperationReturnTemplate2 {

    // We now actually have a list of return type
    // And a list of Assignment CompatibleTypes
    interface Adaptors {

        // [[RawType1, RawType2], [Type1, Type2]]
        OperationReturnTemplate2 returnClass(Type... assingableToAnyOf);

        // baseClass must be a common class of specified types
        // (Object, String) -> Only strings are allowed but we adaptTo specified class
        // adaptTo must be assignable to all specified types
        // [CommonType, [Type1, Type2]]
        OperationReturnTemplate2 returnCommonClass(Class<?> commonType, Type... assingableToAnyOf);

        // Returns an object. Will fail if void method // Methods annotated with @Provide/@Get cannot have void return type
        // [Object, Object]
        default OperationReturnTemplate2 returnObject() {
            return returnClass(Object.class, Object.class);
        }

        /**
         * Checks that that the operation (typically method) returns void. Or maybe it is a check, we do not adaption
         */
        // Er det her ikke bare checksReturn, jo det vil jeg mene
        // [void, void]
        default OperationReturnTemplate2 returnVoid() {
            return returnClass(void.class, void.class);
        }

        // Will ignore any return type
        // [void, Object]
        default OperationReturnTemplate2 returnVoidIgnoreActual() {
            return returnClass(void.class, Object.class);
        }

        // It can A be a sealed interface with records, b be a record () which you can then add methods to
        /**
         * The specified class can be either
         *
         * B: a record class with one parameter
         *
         * @param sealedRecords
         * @return
         */
        // Extract, ValidateOnly
        OperationReturnTemplate2 returnRecordWrapper(Class<?> sealedRecords);

        /**
         *
         * A sealed interface where all permitted classes are records with a zero or a single parameter. 0 parameters indicates
         * that void may be returned. 1 parameters indicates one valid return value.
         *
         * @param sealedRecords
         * @return
         * @throws IllegalArgumentException not sealed, not interface, not record, not 1 0, more than 1,
         * mutable assignableType
         */
        OperationReturnTemplate2 returnSealedRecordWrapper(Class<?> sealedRecords);
    }
    // Each record can have 0 or 1 parameters (Only 1 with 0 thorugh)
    // Will checkReturnAnyOf.
    // Will MT OneType->SealedType

    interface Checks {
        // TypeVariables cannot be specified
        OperationReturnTemplate2 checkReturns(Type... anyOf);

        // Checks that the operation returns a valid key

        // TypeVariables cannot be specified
        @SuppressWarnings("unchecked")
        OperationReturnTemplate2 checkReturnsRecordComponent(Class<? extends Record>... Of);

        // Should probably parse and save the key, because 99.9% of the time they will call .asKey();
        // Then maybe just call toKey();???
        OperationReturnTemplate2 checkReturnsKey(); // can be combined with other check methods to check type

        // Method Only
        OperationReturnTemplate2 checkReturnsVoid();
    }

    // Checks if the return value can be assigned to any of the components.
    // Checks assignment compatible with any of the record components.
    // But does not modify the return type

}
