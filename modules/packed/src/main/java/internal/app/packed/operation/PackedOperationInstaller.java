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

import java.util.function.Function;

import app.packed.extension.ExtensionPoint.UseSite;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationTemplate.Installer;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;

/**
 *
 */
public abstract non-sealed class PackedOperationInstaller implements OperationTemplate.Installer {

    public final BeanSetup bean;

    public EmbeddedIntoOperation embeddedInto;

    public String namePrefix = "tbd";

    public OperationHandle<?> oh;

    public final OperationType operationType;

    public final ExtensionSetup operator;

    public PackedOperationTarget pot;

    public final PackedOperationTemplate template;

    public OperationSetup os;

    public PackedOperationInstaller(PackedOperationTemplate template, OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        this.template = template;
        this.operationType = operationType;
        this.bean = bean;
        this.operator = operator;
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

    /**
     * @return
     */
    public OperationHandle<?> initializeOperationConfiguration() {
        return requireNonNull(oh);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle<?> install(OperationTemplate template) {
        throw new UnsupportedOperationException();
    }

    public OperationSetup newOperation(Function<? super Installer, OperationHandle<?>> newHandle) {
        checkConfigurable();
        OperationSetup os = new OperationSetup(this, pot);
        this.os = os;
        OperationHandle<?> h = newHandle.apply(this);
        os.handle = h;
        os.bean.operations.add(os);
        return os;
    }
}
