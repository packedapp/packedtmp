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
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.bean.sidebean.SidebeanConfiguration;
import app.packed.context.Context;
import app.packed.extension.ExtensionPoint.ExtensionPointHandle;
import app.packed.namespace.NamespaceHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationType;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanIntrospectorSetup;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;
import internal.app.packed.operation.PackedOperationTemplate.ReturnKind;

/**
 *
 */
public non-sealed class PackedOperationInstaller extends AbstractComponentInstaller<OperationSetup, PackedOperationInstaller> implements OperationInstaller {

    NamespaceHandle<?, ?> addToNamespace;

    /** The bean the operation is being installed into. */
    final BeanSetup bean;

    EmbeddedIntoOperation embeddedInto;

    String namePrefix = "tbd";

    PackedOperationTarget operationTarget;

    BeanSetup attachToSidebean;

    /** The type of the operation. */
    final OperationType operationType;

    /** The extension that is installing the operation. */
    final ExtensionSetup operator;

    /** The template of the operation. */
    PackedOperationTemplate template;

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
    public OperationInstaller delegateTo(ExtensionPointHandle extension) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> handleFactory) {
        return (H) newOperation((Function<? super OperationInstaller, OperationHandle<?>>) handleFactory).handle();
    }

    @Override
    public <H extends OperationHandle<?>, N extends NamespaceHandle<?, ?>> H install(N namespace, BiFunction<? super OperationInstaller, N, H> factory) {
        checkNotUsed();
        this.addToNamespace = requireNonNull(namespace);
        return install(f -> factory.apply(f, namespace));
    }

    OperationSetup newOperation(Function<? super OperationInstaller, OperationHandle<?>> newHandle) {
        return OperationSetup.newOperation(this, newHandle);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <H extends OperationHandle<?>> OperationSetup newOperationFromMember(OperationMemberTarget<?> member, MethodHandle directMH,
            Function<? super OperationInstaller, H> configurationCreator) {
        namePrefix = member.name();

        operationTarget = new MemberOperationTarget(member, directMH);

        return newOperation((Function) configurationCreator);
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationInstaller attachToSidebean(SidebeanConfiguration<?> configuration) {
        if (attachToSidebean != null) {
            throw new IllegalStateException("A sidebean has already been attached");
        }
        attachToSidebean = BeanSetup.crack(requireNonNull(configuration));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationInstaller addContext(Class<? extends Context<?>> contextClass) {
        template = template.withContext(contextClass);
        return this;
    }

    /** {@inheritDoc} */
    public PackedOperationInstaller template(PackedOperationTemplate template) {
        PackedOperationTemplate t = template;
        if (t.returnKind == ReturnKind.DYNAMIC) {
            t = t.withReturnType(operationType.returnRawType());
        }
        this.template = t;
        return this;
    }

    @Override
    public PackedOperationInstaller returnDynamic() {
        return template(template.withReturnTypeDynamic());
    }

    /** {@inheritDoc} */
    @Override
    public PackedOperationInstaller returnIgnore() {
        return template(template.withReturnIgnore());
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller returnType(Class<?> type) {
        return template(template.withReturnType(type));
    }

    public static PackedOperationInstaller newInstaller(BeanIntrospectorSetup extension, MethodHandle directMH, OperationMemberTarget<?> target,
            OperationType operationType) {
        return new PackedOperationInstaller(PackedOperationTemplate.DEFAULTS, operationType, extension.scanner.bean, extension.extension()) {

            @SuppressWarnings("unchecked")
            @Override
            public final <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> handleFactory) {
                OperationSetup operation = newOperationFromMember(target, directMH, handleFactory);
                extension.scanner.unBoundOperations.add(operation);
                return (H) operation.handle();
            }
        };
    }
}
