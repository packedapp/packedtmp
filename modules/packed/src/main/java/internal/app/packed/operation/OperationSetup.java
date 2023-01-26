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
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
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
import internal.app.packed.binding.BindingProvider.FromOperation;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.binding.ExtensionServiceBindingSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.FieldOperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.MethodOperationSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** Represents an operation on a bean. */
public sealed abstract class OperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), OperationMirror.class, "initialize", void.class,
            OperationSetup.class);

    /** A MethodHandle for creating a new handle {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_NEW_OPERATION_HANDLE = LookupUtil.findConstructor(MethodHandles.lookup(), OperationHandle.class, OperationSetup.class);

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] NO_BINDINGS = new BindingSetup[0];

    /** A handle that can access OperationHandle#operation. */
    private static final VarHandle VH_OPERATION_HANDLE_CRACK = LookupUtil.findVarHandle(MethodHandles.lookup(), OperationHandle.class, "operation",
            OperationSetup.class);

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** By who this operation is invoked */
    public InvocationSite invocationSite;

    /** The operation's template. */
    public final PackedOperationTemplate template;

    /** Whether or not this operation can still be configured. */
    public boolean isClosed;

    /** Whether or not an invoker has been computed */
    boolean isComputed;

    // Maybe store it in subclasses?
    public MethodHandle methodHandle;

    private MethodHandle generatedMethodHandle;

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    /** The name of the operation */
    public String name; // name = operator.simpleName + "Operation"

    public BindingSetup onBinding;

    // Not final atm because, we might allow an extension to transfer ownership to another extension
    // What about composites?
    public ExtensionSetup operator;

    /** The type of this operation. */
    public final OperationType type;

    @Nullable
    public OperationSetup parent;

    private OperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType type, OperationTemplate template) {
        this.operator = requireNonNull(operator);
        this.type = requireNonNull(type);
        this.bean = requireNonNull(bean);
        this.bindings = type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[type.parameterCount()];
        this.template = requireNonNull((PackedOperationTemplate) template);
    }

    public final Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        forEachBinding(b -> {
            if (b instanceof ExtensionServiceBindingSetup s) {
                requireNonNull(s.extensionBean);
                result.add(s.extensionBean);
            } else if (b instanceof ServiceBindingSetup s) {
                if (s.entry.provider != null) {
                    result.add(s.entry.provider.bean);
                }
            }
        });
        return result;
    }

    // Ideen er vi har et raw method, og naar vi returnere
    // Skulle det gerne vaere et method handle der matcher template.invocationType
    protected MethodHandle doBuild() {
        MethodHandle mh = methodHandle;

//        System.out.println("-------------");
//        System.out.println("Generating MethodHandle for " + type);
//        System.out.println("InvocationType : " + template.invocationType());
//        System.out.println(getClass());
//        System.out.println();
        if (this instanceof BeanAccessOperationSetup baos) {
            return baos.bean.accessBeanX().provideSpecial();
        }

        // System.out.println(mh.type() + " " + site);

//        System.out.println("--------Build Invoker-------------------");
//        System.out.println("Bean = " + bean.path() + ", operation = " + name + ", target = " + target.getClass().getSimpleName());
//        System.out.println(type);
//        System.out.println("Building Operation [bean = " + bean.path() + ": " + mh);

        // Whether or not we need the bean instance
        boolean requiresBeanInstance = false;
        if (this instanceof MethodOperationSetup s) {
            requiresBeanInstance = !Modifier.isStatic(s.modifiers());
        } else if (this instanceof FieldOperationSetup s) {
            requiresBeanInstance = !Modifier.isStatic(s.modifiers());
        }

        int osInit = 0;
        if (requiresBeanInstance) {
            mh = MethodHandles.collectArguments(mh, 0, bean.accessBeanX().provideSpecial());
            osInit = 1;
        } else if (bindings.length == 0) {
            return MethodHandles.dropArguments(mh, 0, template.invocationType().parameterArray());
        }

        if (bindings.length > 0) {
            Osi osi = new Osi();
            osi.nextIndex = osInit;
            osi.mh = mh;
            osi.process(this);
            mh = osi.mh;
        }

        return mh;
    }

    // readOnly. Will not work if for example, resolving a binding
    public final void forEachBinding(Consumer<? super BindingSetup> binding) {
        for (BindingSetup bs : bindings) {
            if (bs == null) {
                System.out.println(type + " ");
            }
            if (bs.provider != null && bs.provider instanceof FromOperation nested) {
                nested.operation.forEachBinding(binding);
            }
            binding.accept(bs);
        }
    }

    public final MethodHandle generateMethodHandle() {
        bean.container.application.checkInCodegenPhase();
        // Burde kun skulle laves en gang, men nogle forskellige steder der kalder
        // den metode, skal lige finde ud af hvorfra
        if (generatedMethodHandle != null) {
            return generatedMethodHandle;
        }

        MethodHandle mh = doBuild();

        requireNonNull(mh);
        if (!mh.type().equals(template.methodType)) {
            System.err.println("OperationType " + this.toString());
            System.err.println("Expected " + template.methodType);
            System.err.println("Actual " + mh.type());
            throw new Error();
        }
        return generatedMethodHandle = mh;
    }

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

    /** {@return an operation handle for this operation.} */
    public final OperationHandle toHandle() {
        try {
            return (OperationHandle) MH_NEW_OPERATION_HANDLE.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
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
        return (OperationSetup) VH_OPERATION_HANDLE_CRACK.get(handle);
    }

    /** An operation that invokes the abstract method on a {@link FunctionalInterface}. */
    public static final class FunctionOperationSetup extends OperationSetup implements OperationTarget.OfFunction {

        private final Method implementationMethod;

        private final SamType samType;

        /**
         * @param operator
         * @param site
         */
        public FunctionOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                MethodHandle methodHandle, SamType samType, Method implementationMethod) {
            super(operator, bean, operationType, template);
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
    }

    /** An operation that returns the bean instance the operation is defined on. */
    public static final class BeanAccessOperationSetup extends OperationSetup {

        /**
         * @param operator
         * @param site
         */
        public BeanAccessOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template) {
            super(operator, bean, operationType, template);
            name = "InstantAccess";
        }
    }

    /** An operation that invokes or accesses a {@link Member}. */
    public sealed static abstract class MemberOperationSetup<T extends Member> extends OperationSetup {
        /** The {@link Member member}. */
        final T member;

        private MemberOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template, T member) {
            super(operator, bean, operationType, template);
            this.member = requireNonNull(member);
        }

        /** @see Member#getModifiers(). */
        public final int modifiers() {
            return member.getModifiers();
        }

        /** An operation that invokes an underlying {@link Method}. */
        public static final class ConstructorOperationSetup extends MemberOperationSetup<Constructor<?>> implements OperationTarget.OfConstructor {

            /**
             * @param operator
             * @param site
             */
            public ConstructorOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationTemplate template, Constructor<?> constructor,
                    MethodHandle methodHandle) {
                super(operator, bean, OperationType.ofExecutable(constructor), template, constructor);
                this.methodHandle = methodHandle;
                name = "constructor";
                mirrorSupplier = BeanFactoryMirror::new;
            }

            /** {@inheritDoc} */
            @Override
            public Constructor<?> constructor() {
                return member;
            }

            public String toString() {
                return "Constructor: " + StringFormatter.formatSimple(constructor());
            }
        }

        /** An operation that can access an underlying {@link Field}. */
        public static final class FieldOperationSetup extends MemberOperationSetup<Field> implements OperationTarget.OfField {

            /** The way we access the field. */
            private final AccessMode accessMode;

            /**
             * @param operator
             * @param site
             */
            public FieldOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                    MethodHandle methodHandle, Field field, AccessMode accessMode) {
                super(operator, bean, operationType, template, field);
                this.methodHandle = methodHandle;
                this.accessMode = requireNonNull(accessMode);
            }

            /** {@inheritDoc} */
            @Override
            public AccessMode accessMode() {
                return accessMode;
            }

            /** {@inheritDoc} */
            @Override
            public Field field() {
                return member;
            }

            public String toString() {
                return "Field " + StringFormatter.format(member) + " (AccessMode + " + accessMode + ")";
            }
        }

        /** An operation that invokes an underlying {@link Method}. */
        public static final class MethodOperationSetup extends MemberOperationSetup<Method> implements OperationTarget.OfMethod {

            /**
             * @param operator
             * @param site
             */
            public MethodOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType type, OperationTemplate template, Method method,
                    MethodHandle methodHandle) {
                super(operator, bean, type, template, method);
                this.methodHandle = methodHandle;
            }
            // MH -> mirror - no gen
            // MH -> Gen - With caching (writethrough to whereever the bean cache it)
            // MH -> LazyGen - With caching

            /** {@inheritDoc} */
            @Override
            public Method method() {
                return member;
            }

            public String toString() {
                return "Method " + StringFormatter.format(member);
            }
        }
    }

    /** An operation that invokes a method handle. */
    public static final class MethodHandleOperationSetup extends OperationSetup implements OperationTarget.OfMethodHandle {

        /**
         * @param operator
         * @param site
         */
        public MethodHandleOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, OperationTemplate template,
                MethodHandle methodHandle) {
            super(operator, bean, operationType, template);
            this.methodHandle = methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public MethodType methodType() {
            return type.toMethodType();
        }
    }
}
