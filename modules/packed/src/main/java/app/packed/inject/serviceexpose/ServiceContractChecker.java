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
package app.packed.inject.serviceexpose;

import app.packed.validate.Validator;

/**
 *
 */
//ServiceContract expected, ServiceContract actual 
//Wirelets kan ikke lave noget om
// De kan ikke lave noget om
// Verifier, Checker, 

// Hvad hvis bare gerne vil checke at foo ikke er med..

// ??? How does this play out with the generic validation framework??????

// no class and ServiceContract.exactChecker
public abstract class ServiceContractChecker implements Validator<ServiceContract> {

    // checkNot
    // checkIs
    // checkFoo

    public static Validator<ServiceContract> exact(ServiceContract sc) {
        throw new UnsupportedOperationException();
    }
}
// Vi var
//implements ContractChecker (expected, actual)
// Men maaske vil man gerne lave nogle andre checks...
