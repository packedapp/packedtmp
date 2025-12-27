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
package internal.app.packed.bean.sidebean;

import static java.util.Objects.requireNonNull;

import app.packed.operation.OperationHandle;
import app.packed.util.Nullable;
import internal.app.packed.invoke.OperationCodeGenerator;

/**
 * An operation handle that can either be a primary bean or an applied sidebean.
 */
public class SomeOperationHandle<H extends OperationHandle<?>> {

    public final H handle;

    @Nullable
    public final PackedSidebeanAttachment sidebean;

    /** Holds generated code for the operation. */
    public final OperationCodeGenerator codeHolder;

    public SomeOperationHandle(H operationHandle) {
        this.handle = requireNonNull(operationHandle);
        this.sidebean = null;
        codeHolder = new OperationCodeGenerator(this);
    }

    public SomeOperationHandle(H operationHandle, PackedSidebeanAttachment sidebean) {
        this.handle = requireNonNull(operationHandle);
        this.sidebean = requireNonNull(sidebean);
        codeHolder = new OperationCodeGenerator(this);
    }
}
