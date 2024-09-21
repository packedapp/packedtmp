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

import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.namespace.NamespaceHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationTemplate.Installer;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;

/**
 *
 */
public abstract non-sealed class PackedOperationInstaller implements OperationTemplate.Installer {

    @Override
    public <H extends OperationHandle<?>, N extends NamespaceHandle<?, ?>> H install(N namespace, BiFunction<? super Installer, N, H> factory) {
        checkConfigurable();
        this.addToNamespace = requireNonNull(namespace);
        return install(f -> factory.apply(f, namespace));
    }

    NamespaceHandle<?, ?> addToNamespace;

    public final BeanSetup bean;

    public EmbeddedIntoOperation embeddedInto;

    public String namePrefix = "tbd";

    public OperationHandle<?> oh;

    OperationSetup operation;

    public final OperationType operationType;

    public final ExtensionSetup operator;

    public PackedOperationTarget pot;

    public final PackedOperationTemplate template;

    public PackedOperationInstaller(PackedOperationTemplate template, OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        this.template = template;
        this.operationType = operationType;
        this.bean = bean;
        this.operator = operator;
    }

    /**
     *
     */
    void checkConfigurable() {}

    /** {@inheritDoc} */
    @Override
    public OperationTemplate.Installer delegateTo(ExtensionUseSite extension) {
        return null;
    }

    /**
     * @return
     */
    public OperationHandle<?> initializeOperationConfiguration() {
        return requireNonNull(oh);
    }

    OperationSetup newOperation(Function<? super Installer, OperationHandle<?>> newHandle) {
        return OperationSetup.newOperation(this, newHandle);
    }

    public static OperationSetup crack(OperationTemplate.Installer installer) {
        return ((PackedOperationInstaller) installer).operation;
    }
}
