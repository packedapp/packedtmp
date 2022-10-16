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

import app.packed.base.Nullable;
import app.packed.operation.InvocationType;
import internal.app.packed.container.ExtensionSetup;

/** The invocation site of an operation. */
public final class InvocationSite {
    // Add info about context I think

    // Nested operations have the same invocation site
    
    // Do we compute something lazily?
    
    /** The invocation type for this operation. */
    @Nullable // for bean access? Maybe just an empty type
    public final InvocationType invocationType;

    /** The extension that operates the operation. MethodHandles will be generated relative to this. */
    public final ExtensionSetup operator;

    public InvocationSite(InvocationType invocationType, ExtensionSetup operator) {
        this.invocationType = invocationType;
        this.operator = operator;
    }
}