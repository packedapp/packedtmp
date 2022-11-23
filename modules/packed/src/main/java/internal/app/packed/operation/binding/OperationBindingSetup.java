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
package internal.app.packed.operation.binding;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.container.User;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class OperationBindingSetup extends BindingSetup {

    /** The operation that will produce values for the binding. */
    public final OperationSetup providingOperation;

    public OperationBindingSetup(OperationSetup operation, int index, User user, BindingOrigin target, OperationSetup providingOperation) {
        super(operation, index, user, target);
        this.providingOperation = requireNonNull(providingOperation);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
        MethodHandle mh = providingOperation.generateMethodHandle();
        return MethodHandles.collectArguments(methodHandle, index, mh);
    }
}
