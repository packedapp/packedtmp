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
package sandbox.extension.operation;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint;
import app.packed.operation.OperationTarget;
import app.packed.util.OperationType;
import internal.app.packed.operation.PackedDelegatingOperationHandle;

/**
 * A special type of operation handle that allows an extension to delegate the execution of an operation to another
 * extension.
 *
 * @see app.packed.bean.BeanIntrospector.OperationalMethod#newDelegatingOperation()
 */
public sealed interface DelegatingOperationHandle permits PackedDelegatingOperationHandle {

    /** {@return the extension that created this handle.} */
    Class<? extends Extension<?>> delegatedFrom();

//    /**
//     * {@return @{code true} if an operation has been created from this handle, otherwise false.}
//     */
//    boolean isDelegated();

    OperationHandle newOperation(OperationTemplate template, ExtensionPoint.UseSite useSite);

    /** {@return the target of this operation.} */
    OperationTarget target();

    /** {@return the type of the operation.} */
    OperationType type();
}
