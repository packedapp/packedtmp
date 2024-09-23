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

import app.packed.bean.BeanFactoryConfiguration;
import app.packed.bean.BeanFactoryMirror;
import app.packed.bean.BeanKind;
import app.packed.bean.CannotDeclareInstanceMemberException;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.namespace.NamespaceHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationTemplate.Installer;
import app.packed.operation.OperationType;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.component.PackedComponentInstaller;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;

/**
 *
 */
public non-sealed class PackedOperationInstaller extends PackedComponentInstaller<OperationSetup, PackedOperationInstaller>
        implements OperationTemplate.Installer {

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
    public OperationTemplate.Installer delegateTo(ExtensionUseSite extension) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <H extends OperationHandle<?>> H install(Function<? super Installer, H> handleFactory) {
        return (H) newOperation((Function<? super Installer, OperationHandle<?>>) handleFactory).handle();
    }

    @Override
    public final <H extends OperationHandle<?>, N extends NamespaceHandle<?, ?>> H install(N namespace, BiFunction<? super Installer, N, H> factory) {
        checkNotInstalledYet();
        this.addToNamespace = requireNonNull(namespace);
        return install(f -> factory.apply(f, namespace));
    }

    final OperationSetup newOperation(Function<? super Installer, OperationHandle<?>> newHandle) {
        return OperationSetup.newOperation(this, newHandle);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <H extends OperationHandle<?>> OperationSetup newOperationFromMember(PackedOperationInstaller installer, OperationMemberTarget<?> member,
            MethodHandle methodHandle, Function<? super OperationTemplate.Installer, H> configurationCreator) {
        if (installer.bean.beanKind == BeanKind.STATIC && !Modifier.isStatic(member.modifiers())) {
            throw new CannotDeclareInstanceMemberException("Cannot create operation for non-static member " + member);
        }
        installer.namePrefix = member.name();

        installer.operationTarget = new MemberOperationTarget(member, methodHandle);

        OperationSetup os = installer.newOperation((Function) configurationCreator);
        return os;
    }

    /** A bean handle for a factory method. */
    public static final class BeanFactoryOperationHandle extends OperationHandle<BeanFactoryConfiguration> {

        /**
         * @param installer
         */
        public BeanFactoryOperationHandle(OperationTemplate.Installer installer) {
            super(installer);
        }

        @Override
        protected BeanFactoryConfiguration newOperationConfiguration() {
            return new BeanFactoryConfiguration(this);
        }

        @Override
        protected OperationMirror newOperationMirror() {
            return new BeanFactoryMirror(this);
        }
    }
}
