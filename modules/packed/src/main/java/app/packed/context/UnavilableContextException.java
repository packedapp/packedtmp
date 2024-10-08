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
package app.packed.context;

import app.packed.build.BuildException;

/**
 * When attempting to use {@link ContextualServiceProvider} or
 * {@link app.packed.bean.BeanTrigger.InheritableContextualServiceProvider} and a required context is not available.
 */

// OperationNotInContext?

// The bean is in context, but the operation is not
// Ville vaere en god fejlbeskrivelse for bean contexts..
// Maaske kan beans slet ikke vaere i contexts...

// Hmm StaticContextUnavilable?
// TransactionContext.get()

// ContextNotAvailable
// ProvisionException
// ContextUnavailable
// OutOfContextException
//NotInContextException

// Hvad med generic ting der ikke passer
public class UnavilableContextException extends BuildException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public UnavilableContextException(String message) {
        super(message);
    }
}
