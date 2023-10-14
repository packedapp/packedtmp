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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import app.packed.extension.ExtensionPoint.UseSite;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationHandle.Builder;
import sandbox.extension.operation.OperationTemplate;

/**
 *
 */
public final class PackedOperationBuilder implements OperationHandle.Builder {

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    public PackedOperationBuilder(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
            OperationMemberTarget<?> member, MethodHandle methodHandle) {

    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle build(OperationTemplate template) {
        return null;
    }

    /**
     *
     */
    private void checkConfigurable() {}

    /** {@inheritDoc} */
    @Override
    public Builder delegateTo(UseSite extension) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationBuilder specializeMirror(Supplier<? extends OperationMirror> supplier) {
        checkConfigurable();
        this.mirrorSupplier = requireNonNull(supplier, "supplier is null");
        return this;
    }
}
