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

import app.packed.extension.ExtensionPoint.UseSite;
import app.packed.framework.Nullable;
import app.packed.operation.DelegatingOperationHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;

/**
 *
 */
public final class PackedDelegatingOperationHandle implements DelegatingOperationHandle {

    public final BeanSetup bean;

    public final ExtensionSetup delegatedFrom;
    public final MethodHandle methodHandle;
    @Nullable
    OperationSetup operation;

    public final OperationType operationType;

    public final OperationMemberTarget<?> target;

    public PackedDelegatingOperationHandle(ExtensionSetup delegatedFrom, BeanSetup bean, OperationMemberTarget<?> target, OperationType operationType,
            MethodHandle methodHandle) {
        this.target = requireNonNull(target);
        this.delegatedFrom = requireNonNull(delegatedFrom);
        this.bean = requireNonNull(bean);
        this.methodHandle = methodHandle;
        this.operationType = requireNonNull(operationType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDelegated() {
        return operation != null;
    }

    public OperationHandle newOperation(ExtensionSetup extension, OperationTemplate template) {
        // checkConfigurable
        OperationSetup os = this.operation = new MemberOperationSetup(extension, bean, operationType, template, target, methodHandle);
        bean.operations.add(os);
        bean.introspecting.unBoundOperations.add(os);
        return os.toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(UseSite context, OperationTemplate template) {
        PackedExtensionPointContext c = (PackedExtensionPointContext) context;
        return newOperation(c.usedBy(), template);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTarget target() {
        return (OperationTarget) target;
    }

    /** {@inheritDoc} */
    @Override
    public OperationType type() {
        return operationType;
    }
}
