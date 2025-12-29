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
package internal.app.packed.lifecycle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import internal.app.packed.bean.sidebean.PackedSidebeanAttachment;
import internal.app.packed.invoke.OperationCodeGenerator;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class SomeLifecycleOperationHandle<H extends LifecycleOperationHandle> {

    public MethodHandle methodHandle;

    public final H handle;

    /** Holds generated code for the operation. */
    public final OperationCodeGenerator codeHolder;

    public SomeLifecycleOperationHandle(OperationSetup operation, H operationHandle, PackedSidebeanAttachment sidebean) {
        this.handle = requireNonNull(operationHandle);
        this.codeHolder = new OperationCodeGenerator(operation, sidebean);
    }

    /**
     * @return
     */
    public PackedBeanLifecycleKind lifecycleKind() {
        return handle.lifecycleKind;
    }
}
