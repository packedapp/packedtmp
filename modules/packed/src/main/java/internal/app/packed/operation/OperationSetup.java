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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanKind;
import app.packed.bean.CannotDeclareInstanceMemberException;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.context.Context;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanScanner;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution.FromOperationResult;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.component.PackedComponentTwin;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.entrypoint.EntryPointSetup;
import internal.app.packed.operation.PackedOperationType.MemberOperationSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.types.ClassUtil;
import sandbox.extension.operation.OperationHandle;

/** Represents an operation on a bean. */
public final class OperationSetup extends ComponentSetup implements PackedComponentTwin , ContextualizedElementSetup {

    /** A magic initializer for {@link OperationMirror}. */
    public static final MagicInitializer<OperationSetup> MIRROR_INITIALIZER = MagicInitializer.of(OperationMirror.class);

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] NO_BINDINGS = {};

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** The configuration representing this operation, is set from {@link #initConfiguration(BeanConfiguration)}. */
    @Nullable
    public OperationConfiguration configuration;

    /** The contexts on the bean. Is HashMap now we because it uses less memory when empty. */
    private HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** Any operation this operation is embedded into. */
    @Nullable
    public final EmbeddedIntoOperation embeddedInto;

    @Nullable
    public final EntryPointSetup entryPoint;

    /** The generated method handle. */
    @Nullable
    private MethodHandle generatedMethodHandle;

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

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
    String namePrefix; // name = operator.simpleName + "Operation"

    /** The operator of the operation. */
    public final ExtensionSetup operator;

    /** The operation's template. */
    public final PackedOperationTemplate template;

    /** The type of this operation. */
    public final OperationType type;

    public final PackedOperationType pot;

    OperationSetup(PackedOperationInstaller installer, PackedOperationType pot) {
        this.operator = requireNonNull(installer.operator);
        this.pot = pot;
        this.bean = requireNonNull(installer.bean);
        this.type = installer.operationType;
        this.embeddedInto = installer.embeddedInto;
        this.bindings = type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[type.parameterCount()];
        this.template = installer.template;

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

    /**
     * Initializes the bean configuration.
     *
     * @param configuration
     *
     * @throws IllegalStateException
     *             if attempting to create multiple bean configurations for a single bean
     */
    public void initConfiguration(OperationConfiguration configuration) {
        if (this.configuration != null) {
            throw new IllegalStateException("A operation handle can only be used once to create a a operation configuration");
        }
        this.configuration = requireNonNull(configuration);
    }

    /** {@return the initial method handle.} */
    public MethodHandle methodHandle() {
        return pot.methodHandle();
    }

    /** {@return a new mirror.} */
    @Override
    public OperationMirror mirror() {
        return MIRROR_INITIALIZER.run(() -> ClassUtil.newMirror(OperationMirror.class, OperationMirror::new, mirrorSupplier), this);
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

    public PackedOperationHandle toHandle() {
        return new PackedOperationHandle(this, null);
    }

    /** {@return an operation handle for this operation.} */
    public PackedOperationHandle toHandle(BeanScanner scanner) {
        return new PackedOperationHandle(this, scanner);
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
    public static OperationSetup crack(OperationHandle handle) {
        return ((PackedOperationHandle) handle).operation();
    }

    public static OperationSetup newMemberOperationSetup(PackedOperationInstaller installer, OperationMemberTarget<?> member, MethodHandle methodHandle) {
        if (installer.bean.beanKind == BeanKind.STATIC && !Modifier.isStatic(member.modifiers())) {
            throw new CannotDeclareInstanceMemberException("Cannot create operation for non-static member " + member);
        }
//        super.namePrefix = member.name();
//        if (member instanceof OperationConstructorTarget) {
//            super.mirrorSupplier = BeanFactoryMirror::new;
//        }

        MemberOperationSetup mos = new MemberOperationSetup(member, methodHandle);

        return installer.newOperation(mos);
    }

    /** The parent of a nested operation. */
    public /* primitive */ record EmbeddedIntoOperation(OperationSetup operation, int bindingIndex) {}
}
