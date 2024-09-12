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

import java.util.function.Supplier;

import app.packed.extension.ExtensionPoint.UseSite;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;

/**
 *
 */
public final class PackedOperationInstaller implements OperationTemplate.Installer {

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    public final PackedOperationTemplate template;

    public final OperationType operationType;

    public final BeanSetup bean;

    public final ExtensionSetup operator;

    public EmbeddedIntoOperation embeddedInto;

    PackedOperationInstaller(PackedOperationTemplate template, OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        this.template = template;
        this.operationType = operationType;
        this.bean = bean;
        this.operator = operator;
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle install(OperationTemplate template) {
        return null;
    }

    /**
     *
     */
    private void checkConfigurable() {}

    /** {@inheritDoc} */
    @Override
    public OperationTemplate.Installer delegateTo(UseSite extension) {
        return null;
    }

    public OperationSetup newOperation(PackedOperationType pot) {
        return new OperationSetup(this, pot);
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationInstaller specializeMirror(Supplier<? extends OperationMirror> supplier) {
        checkConfigurable();
        this.mirrorSupplier = requireNonNull(supplier, "supplier is null");
        return this;
    }
}
