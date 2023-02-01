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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanFactoryMirror;
import app.packed.framework.Nullable;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.binding.ExtensionServiceBindingSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationMemberTarget.OperationConstructorTarget;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** Represents an operation on a bean. */
public sealed abstract class OperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), OperationMirror.class, "initialize", void.class,
            OperationSetup.class);

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] NO_BINDINGS = new BindingSetup[0];

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** The generated method handle. */
    @Nullable
    private MethodHandle generatedMethodHandle;

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    /** The operator of the operation. */
    public final ExtensionSetup operator;

    /** Any parent this operation might have. */
    @Nullable
    public final NestedOperationParent parent;

    /** The operation's template. */
    public final PackedOperationTemplate template;

    /** The type of this operation. */
    public final OperationType type;

    /** The name of the operation */
    public String zName; // name = operator.simpleName + "Operation"

    private OperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType type, OperationTemplate template, @Nullable NestedOperationParent parent) {
        this.operator = requireNonNull(operator);
        this.bean = requireNonNull(bean);
        this.type = requireNonNull(type);
        this.parent = parent;
        this.bindings = type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[type.parameterCount()];
        this.template = requireNonNull((PackedOperationTemplate) template);
    }

    public final Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        // TODO hmm, skal inkludere extensions???
        forEachBinding(b -> {
            if (b instanceof ExtensionServiceBindingSetup s) {
                requireNonNull(s.extensionBean);
                result.add(s.extensionBean);

            } else if (b instanceof ServiceBindingSetup s) {
                ServiceProviderSetup provider = s.entry.provider();
                if (provider != null) {
                    result.add(provider.operation.bean);
                }
            }
        });
        return result;
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

    public final MethodHandle generateMethodHandle() {
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
    public final OperationMirror mirror() {
        OperationMirror mirror = ClassUtil.mirrorHelper(OperationMirror.class, OperationMirror::new, mirrorSupplier);

        // Initialize OperationMirror by calling OperationMirror#initialize(OperationSetup)
        try {
            MH_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    /** {@return the target of the operation.} */
    public OperationTarget target() {
        return (OperationTarget) this;
    }

    /** {@return an operation handle for this operation.} */
    public final PackedOperationHandle toHandle() {
        return new PackedOperationHandle(this);
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
            zName = "InstantAccess";
        }

        /** {@inheritDoc} */
        @Override
        MethodHandle methodHandle() {
            throw new UnsupportedOperationException();
        }
    }

    /** An operation that invokes the abstract method on a {@link FunctionalInterface}. */
    public static final class FunctionOperationSetup extends OperationSetup implements OperationTarget.OfFunction {

        private final Method implementationMethod;

        private final MethodHandle methodHandle;

        private final SamType samType;

        /**
         * @param operator
         * @param site
         */
        public FunctionOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                @Nullable NestedOperationParent nestedParent, MethodHandle methodHandle, SamType samType, Method implementationMethod) {
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

        /** The {@link Member member}. */
        public final OperationMemberTarget<?> target;

        // MH -> mirror - no gen
        // MH -> Gen - With caching (writethrough to whereever the bean cache it)
        // MH -> LazyGen - With caching
        public MemberOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                OperationMemberTarget<?> member, MethodHandle methodHandle) {
            super(operator, bean, operationType, template, null);
            this.target = requireNonNull(member);
            this.methodHandle = requireNonNull(methodHandle);
            if (member instanceof OperationConstructorTarget) {
                zName = "constructor";
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
                @Nullable NestedOperationParent nestedParent, MethodHandle methodHandle) {
            super(operator, bean, operationType, template, nestedParent);
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
    public record NestedOperationParent(OperationSetup operation, int bindingIndex) {}
}
