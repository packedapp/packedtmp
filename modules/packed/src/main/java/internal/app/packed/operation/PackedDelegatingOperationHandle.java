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

import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint.UseSite;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanScanner;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationSetup;

/**
 *
 */
public final class PackedDelegatingOperationHandle implements DelegatingOperationHandle {

    /** The bean the operation is created on. */
    public final BeanSetup bean;

    public final ExtensionSetup delegatedFrom;

    public final MethodHandle methodHandle;

    /** The type of the operation */
    public final OperationType operationType;

    /** The target of the operation. */
    public final OperationMemberTarget<?> target;

    final BeanScanner scanner;

    public PackedDelegatingOperationHandle(BeanScanner scanner, ExtensionSetup delegatedFrom, BeanSetup bean, OperationMemberTarget<?> target,
            OperationType operationType, MethodHandle methodHandle) {
        this.scanner = requireNonNull(scanner);
        this.target = requireNonNull(target);
        this.delegatedFrom = requireNonNull(delegatedFrom);
        this.bean = requireNonNull(bean);
        this.methodHandle = methodHandle;
        this.operationType = requireNonNull(operationType);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> delegatedFrom() {
        return delegatedFrom.extensionType;
    }

    public OperationHandle<?> newOperation(ExtensionSetup extension, PackedOperationTemplate template) {
        // checkConfigurable
        PackedOperationInstaller poi = template.newInstaller(operationType, bean, extension);
        poi.pot = new MemberOperationSetup(target, methodHandle);

        OperationSetup os = poi.newOperation(OperationHandle::new);

//        bean.operations.all.add(os);
        scanner.unBoundOperations.add(os);
        return os.handle();
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle<?> newOperation(OperationTemplate template, UseSite context) {
        PackedExtensionPointContext c = (PackedExtensionPointContext) context;
        return newOperation(c.usedBy(), (PackedOperationTemplate) template);
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
