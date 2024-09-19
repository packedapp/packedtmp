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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.bean.BeanKind;
import app.packed.bean.CannotDeclareInstanceMemberException;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.context.Context;
import app.packed.extension.InternalExtensionException;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationTemplate.Installer;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution.FromOperationResult;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.entrypoint.EntryPointSetup;
import internal.app.packed.namespace.NamespaceSetup;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.util.LookupUtil;

/** Represents an operation on a bean. */
public final class OperationSetup implements ComponentSetup , ContextualizedElementSetup {

    /** A handle that can access {@link OperationHandle#handle}. */
    private static final VarHandle VH_OPERATION_HANDLE_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), OperationHandle.class, "operation",
            OperationSetup.class);

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** The contexts on the bean. Is HashMap now we because it uses less memory when empty. */
    private final HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** Any operation this operation is embedded into. */
    @Nullable
    public final EmbeddedIntoOperation embeddedInto;

    @Nullable
    public final EntryPointSetup entryPoint;

    /** The generated method handle. */
    @Nullable
    private MethodHandle generatedMethodHandle;

    /** The operation's handle. */
    private OperationHandle<?> handle;

    /**
     * The name prefix of the operation.
     * <p>
     * Multiple operations can have the same
     */
    // Could also have a calculated name
    // Record Name(int state, String name)
    // state = 0 = generated, Integer.max = final, anywhere in between the number of
    // operations in the bean when the name was calculated
    // Det hele er vi ikke gider calculate et navn, med mindre det skal bruges
    // Ved method overloading kan vi risikere at 2 operationer med samme navn
    // Hvilket ikke fungere hvis vi vil have component path operationer
    public String namePrefix; // name = operator.simpleName + "Operation"

    /** The operator of the operation. */
    public final ExtensionSetup operator;

    public final PackedOperationTarget pot;

    /** The operation's template. */
    public final PackedOperationTemplate template;

    /** The type of this operation. */
    public final OperationType type;

    OperationSetup(PackedOperationInstaller installer, PackedOperationTarget pot) {
        this.operator = requireNonNull(installer.operator);
        this.pot = requireNonNull(pot);
        this.bean = requireNonNull(installer.bean);
        this.type = installer.operationType;
        this.embeddedInto = installer.embeddedInto;
        this.bindings = new BindingSetup[type.parameterCount()];
        this.template = installer.template;
        this.namePrefix = installer.namePrefix; // temporarty
        if (template.descriptor().newLifetime()) {
            this.entryPoint = new EntryPointSetup(this, bean.lifetime);
        } else {
            this.entryPoint = null;
        }
        for (PackedContextTemplate t : template.contexts.values()) {
            contexts.put(t.contextClass(), new ContextSetup(t, this));
        }
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return ComponentKind.OPERATION.pathNew(bean.componentPath(), name());
    }

    public Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        // TODO hmm, skal inkludere extensions??? nope
        forEachBinding(b -> {
            if (b instanceof ServiceBindingSetup s) {
                ServiceProviderSetup provider = s.entry.provider();
                if (provider != null) {
                    result.add(provider.operation.bean);
                }
            }
        });
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public ContextSetup findContext(Class<? extends Context<?>> contextClass) {
        Class<? extends Context<?>> cl = ContextInfo.normalize(contextClass);
        ContextSetup cs = contexts.get(cl);
        if (cs != null) {
            return cs;
        }
        return bean.findContext(cl);
    }

    // readOnly. Will not work if for example, resolving a binding
    public void forEachBinding(Consumer<? super BindingSetup> binding) {
        for (BindingSetup bs : bindings) {
            requireNonNull(bs);
            if (bs.resolver() != null && bs.resolver() instanceof FromOperationResult nested) {
                nested.operation().forEachBinding(binding);
            }
            binding.accept(bs);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forEachContext(BiConsumer<? super Class<? extends Context<?>>, ? super ContextSetup> action) {
        contexts.forEach(action);
        bean.forEachContext(action);
    }

    public MethodHandle generateMethodHandle() {
        // Maybe have a check here instead, and specifically mention generateMethodHandle when calling
        bean.container.application.checkInCodegenPhase();
        MethodHandle mh = generatedMethodHandle;
        if (mh == null) {
            mh = generatedMethodHandle = new OperationCodeGenerator().generate(this, methodHandle());
        }
        assert (mh.type() == template.methodType);
        return mh;
    }

    public OperationHandle<?> handle() {
        return requireNonNull(handle);
    }

    /** {@return the initial method handle.} */
    public MethodHandle methodHandle() {
        return pot.methodHandle();
    }

    /** {@return a new mirror.} */
    @Override
    public OperationMirror mirror() {
        return handle().mirror();
    }

    /** {@return the name of the operation} */
    public String name() {
        return bean.operations.operationNames().get(this);
    }

    public String namePrefix() {
        return namePrefix;
    }

    /** {@return the target of the operation.} */
    public OperationTarget target() {
        return pot.target();
    }

    /** {@return the type of operation.} */
    public OperationType type() {
        return type;
    }

    /**
     * Extracts an operation setup from an operation handle.
     *
     * @param handle
     *            the handle to extract from
     * @return the operation setup
     */
    public static OperationSetup crack(OperationHandle<?> handle) {
        return (OperationSetup) VH_OPERATION_HANDLE_TO_SETUP.get(handle);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <H extends OperationHandle<?>> OperationSetup newMemberOperationSetup(PackedOperationInstaller installer, OperationMemberTarget<?> member,
            MethodHandle methodHandle, Function<? super OperationTemplate.Installer, H> configurationCreator) {
        if (installer.bean.beanKind == BeanKind.STATIC && !Modifier.isStatic(member.modifiers())) {
            throw new CannotDeclareInstanceMemberException("Cannot create operation for non-static member " + member);
        }
        installer.namePrefix = member.name();

        // super.namePrefix = member.name();
//        if (member instanceof OperationConstructorTarget) {
//            super.mirrorSupplier = BeanFactoryMirror::new;
//        }

        installer.pot = new MemberOperationSetup(member, methodHandle);

        OperationSetup os = installer.newOperation((Function) configurationCreator);
        return os;
    }

    static OperationSetup newOperation(PackedOperationInstaller installer, Function<? super Installer, OperationHandle<?>> newHandle) {
        installer.checkConfigurable();

        OperationSetup os = new OperationSetup(installer, installer.pot);
        installer.operation = os;

        // Create the OperationHandle
        OperationHandle<?> handle = newHandle.apply(installer);
        if (handle == null) {
            throw new InternalExtensionException(newHandle + " returned null, when creating a new OperationHandle");
        }
        if (installer.addToNamespace != null) {
            NamespaceSetup s = NamespaceSetup.crack(installer.addToNamespace);
            s.operations.add(os);
        }
        os.handle = handle;
        os.bean.operations.add(os);
        return os;
    }

    /** The parent of a nested operation. */
    public /* value */ record EmbeddedIntoOperation(OperationSetup operation, int bindingIndex) {}
}
