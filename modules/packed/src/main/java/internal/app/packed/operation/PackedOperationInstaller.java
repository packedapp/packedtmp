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
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.bean.BeanKind;
import app.packed.bean.scanning.InstanceMembersDisallowedException;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.namespace.NamespaceHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationType;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;

/**
 *
 */
public non-sealed class PackedOperationInstaller extends AbstractComponentInstaller<OperationSetup, PackedOperationInstaller> implements OperationInstaller {

    NamespaceHandle<?, ?> addToNamespace;

    /** The bean the operation is being installed into. */
    public final BeanSetup bean;

    EmbeddedIntoOperation embeddedInto;

    public String namePrefix = "tbd";

    public PackedOperationTarget operationTarget;

    /** The type of the operation. */
    final OperationType operationType;

    /** The extension that is installing the operation. */
    final ExtensionSetup operator;

    /** The template of the operation. */
    final PackedOperationTemplate template;

    public PackedOperationInstaller(PackedOperationTemplate template, OperationType operationType, BeanSetup bean, ExtensionSetup operator) {
        this.template = template;
        this.operationType = operationType;
        this.bean = bean;
        this.operator = operator;
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationSetup application(OperationSetup setup) {
        return setup.bean.container.application;
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller delegateTo(ExtensionUseSite extension) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> handleFactory) {
        return (H) newOperation((Function<? super OperationInstaller, OperationHandle<?>>) handleFactory).handle();
    }

    @Override
    public <H extends OperationHandle<?>, N extends NamespaceHandle<?, ?>> H install(N namespace, BiFunction<? super OperationInstaller, N, H> factory) {
        checkNotInstalledYet();
        this.addToNamespace = requireNonNull(namespace);
        return install(f -> factory.apply(f, namespace));
    }

    OperationSetup newOperation(Function<? super OperationInstaller, OperationHandle<?>> newHandle) {
        return OperationSetup.newOperation(this, newHandle);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <H extends OperationHandle<?>> OperationSetup newOperationFromMember(OperationMemberTarget<?> member, MethodHandle methodHandle,
            Function<? super OperationInstaller, H> configurationCreator) {
        if (bean.beanKind == BeanKind.STATIC && !Modifier.isStatic(member.modifiers())) {
            throw new InstanceMembersDisallowedException("Cannot create operation for non-static member " + member);
        }
        namePrefix = member.name();

        operationTarget = new MemberOperationTarget(member, methodHandle);

        return newOperation((Function) configurationCreator);
    }
}
