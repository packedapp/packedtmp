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
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanFactoryMirror;
import app.packed.bean.BeanKind;
import app.packed.bean.NonStaticMemberException;
import app.packed.context.Context;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.util.Nullable;
import app.packed.util.OperationType;
import internal.app.packed.bean.BeanScanner;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.entrypoint.EntryPointSetup;
import internal.app.packed.operation.OperationMemberTarget.OperationConstructorTarget;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;

/** Represents an operation on a bean. */
public sealed abstract class OperationSetup implements ContextualizedElementSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), OperationMirror.class, "initialize", void.class,
            OperationSetup.class);

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] NO_BINDINGS = {};

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

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

    private OperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType type, OperationTemplate template,
            @Nullable EmbeddedIntoOperation embeddedInto) {
        this.operator = requireNonNull(operator);
        this.bean = requireNonNull(bean);
        this.type = requireNonNull(type);
        this.embeddedInto = embeddedInto;
        this.bindings = type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[type.parameterCount()];
        this.template = requireNonNull((PackedOperationTemplate) template);

        if (template.newLifetime()) {
            this.entryPoint = new EntryPointSetup(this, bean.lifetime);
        } else {
            this.entryPoint = null;
        }
        if (!this.template.contexts.isEmpty()) {
            for (PackedContextTemplate t : this.template.contexts.values()) {
                contexts.put(t.contextClass(), new ContextSetup(t, this));
            }
        }
    }

    public final Set<BeanSetup> dependsOn() {
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
    public final void forEachBinding(Consumer<? super BindingSetup> binding) {
        for (BindingSetup bs : bindings) {
            requireNonNull(bs);
            if (bs.resolver() != null && bs.resolver() instanceof FromOperation nested) {
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

    public final MethodHandle generateMethodHandle() {
        // Maybe have a check here instead, and specifically mention generateMethodHandle when calling
        bean.container.application.checkInCodegenPhase();
        MethodHandle mh = generatedMethodHandle;
        if (mh == null) {
            mh = generatedMethodHandle = new OperationCodeGenerator().generate(this, methodHandle());
        }
        assert (mh.type() == template.methodType);
        return mh;
    }

    /** {@return the initial method handle.} */
    abstract MethodHandle methodHandle();

    /** {@return a new mirror.} */
    @Override
    public final OperationMirror mirror() {
        // debug();
        // new Exception().printStackTrace();
        OperationMirror mirror = ClassUtil.newMirror(OperationMirror.class, OperationMirror::new, mirrorSupplier);

        // Initialize OperationMirror by calling OperationMirror#initialize(OperationSetup)
        try {
            MH_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public String name() {
        return bean.operationNames().get(this);
    }

    public String namePrefix() {
        return "op" + namePrefix;
    }

    /** {@return the target of the operation.} */
    public OperationTarget target() {
        return (OperationTarget) this;
    }

    public final PackedOperationHandle toHandle() {
        return new PackedOperationHandle(this, null);
    }

    /** {@return an operation handle for this operation.} */
    public final PackedOperationHandle toHandle(BeanScanner scanner) {
        return new PackedOperationHandle(this, scanner);
    }

    /** {@return the type of operation.} */
    public final OperationType type() {
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

    /** An operation that returns the bean instance the operation is defined on. */
    public static final class BeanAccessOperationSetup extends OperationSetup {

        /**
         * @param operator
         * @param site
         */
        public BeanAccessOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template) {
            super(operator, bean, operationType, template, null);
            namePrefix = "InstantAccess";
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle methodHandle() {
            throw new UnsupportedOperationException();
        }
    }

    /** An operation that invokes the abstract method on a {@link FunctionalInterface}. */
    public static final class FunctionOperationSetup extends OperationSetup implements OperationTarget.OfFunction {

        /** The method that implements the single abstract method. */
        private final Method implementationMethod;

        private final MethodHandle methodHandle;

        /** A description of SAM type. */
        private final SamType samType;

        /**
         * @param operator
         * @param site
         */
        public FunctionOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                @Nullable EmbeddedIntoOperation nestedParent, MethodHandle methodHandle, SamType samType, Method implementationMethod) {
            super(operator, bean, operationType, template, nestedParent);
            this.methodHandle = requireNonNull(methodHandle);
            this.samType = requireNonNull(samType);
            this.implementationMethod = requireNonNull(implementationMethod);
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> functionalInterface() {
            return samType.functionInterface();
        }

        /** {@inheritDoc} */
        @Override
        public Method implementingMethod() {
            return implementationMethod;
        }

        /** {@inheritDoc} */
        @Override
        public Method interfaceMethod() {
            return samType.saMethod();
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle methodHandle() {
            return methodHandle;
        }
    }

    /** An operation that invokes or accesses a {@link Member}. */
    public static final class MemberOperationSetup extends OperationSetup {

        private final MethodHandle methodHandle;

        /** The {@link Member target member}. */
        public final OperationMemberTarget<?> target;

        // MH -> mirror - no gen
        // MH -> Gen - With caching (writethrough to whereever the bean cache it)
        // MH -> LazyGen - With caching
        public MemberOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                OperationMemberTarget<?> member, MethodHandle methodHandle) {
            super(operator, bean, operationType, template, null);
            if (bean.beanKind == BeanKind.STATIC && !Modifier.isStatic(member.modifiers())) {
                throw new NonStaticMemberException("Cannot create operation for non-static member " + member);
            }
            this.target = requireNonNull(member);
            this.methodHandle = requireNonNull(methodHandle);
            if (member instanceof OperationConstructorTarget) {
                namePrefix = "constructor";
                mirrorSupplier = BeanFactoryMirror::new;
            }
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle methodHandle() {
            return methodHandle;
        }

        public boolean needsBeanInstance() {
            return !(target instanceof OperationConstructorTarget) && !Modifier.isStatic(target.modifiers());
        }

        @Override
        public OperationTarget target() {
            return (OperationTarget) target;
        }
    }

    /** An operation that invokes a method handle. */
    public static final class MethodHandleOperationSetup extends OperationSetup implements OperationTarget.OfMethodHandle {

        final MethodHandle methodHandle;

        /**
         * @param operator
         * @param site
         */
        public MethodHandleOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                @Nullable EmbeddedIntoOperation embeddedInto, MethodHandle methodHandle) {
            super(operator, bean, operationType, template, embeddedInto);
            this.methodHandle = requireNonNull(methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle methodHandle() {
            return methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public MethodType methodType() {
            return type.toMethodType();
        }
    }

    /** The parent of a nested operation. */
    public /* primitive */ record EmbeddedIntoOperation(OperationSetup operation, int bindingIndex) {}
}
