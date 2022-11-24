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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanFactoryMirror;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationSiteMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.IntrospectedBean;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.binding.BindingSetup;
import internal.app.packed.operation.binding.ExtensionServiceBindingSetup;
import internal.app.packed.operation.binding.OperationBindingSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Represents an operation on a bean. */
public sealed abstract class OperationSetup implements OperationSiteMirror {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class, "initialize",
            void.class, OperationSetup.class);

    /** A MethodHandle for creating a new handle {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_NEW_OPERATION_HANDLE = LookupUtil.lookupConstructorPrivate(MethodHandles.lookup(), OperationHandle.class,
            OperationSetup.class, IntrospectedBean.class);

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] NO_BINDINGS = new BindingSetup[0];

    /** A handle that can access OperationHandle#operation. */
    private static final VarHandle VH_OPERATION_HANDLE_CRACK = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), OperationHandle.class, "operation",
            OperationSetup.class);

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** By who this operation is invoked */
    public InvocationSite invocationSite;

    /** The invocation type of this operation. */
    public PackedInvocationType invocationType = PackedInvocationType.DEFAULTS;

    /** Whether or not this operation can still be configured. */
    public boolean isClosed;

    /** Whether or not an invoker has been computed */
    private boolean isComputed;

    // Maybe store it in subclasses?
    public MethodHandle methodHandle;

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    /** The name of the operation */
    public String name; // name = operator.simpleName + "Operation"

    // Not final atm because, we might allow an extension to transfer ownership to another extension
    public ExtensionSetup operator;

    /** The type of this operation. */
    public final OperationType type;

    private OperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType type) {
        this.operator = requireNonNull(operator);
        this.type = requireNonNull(type);
        this.bean = requireNonNull(bean);
        this.bindings = type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[type.parameterCount()];
    }

    public final MethodHandle buildInvoker0() {
        bean.container.application.checkInCodegenPhase();

        if (isComputed) {
            // throw new IllegalStateException("This method can only be called once");
        }

        isComputed = true;

        MethodHandle mh = methodHandle;
        // System.out.println(mh.type() + " " + site);

//        System.out.println("--------Build Invoker-------------------");
//        System.out.println("Bean = " + bean.path() + ", operation = " + name + ", target = " + target.getClass().getSimpleName());
//        System.out.println(type);
//        System.out.println("Building Operation [bean = " + bean.path() + ": " + mh);

        // Vi kan altid have brug for en adjustment...
        // Dependending
        
        if (bindings.length == 0) {
            if (!requiresBeanInstance()) {
                if (mh.type().parameterCount() > 0) {
//                   mh = MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
                    return mh;
                }
                return MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
            }
        }
        // mh = MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);

        if (requiresBeanInstance()) {
            mh = MethodHandles.collectArguments(mh, 0, bean.injectionManager.accessBean(bean));
        }

        for (int i = 0; i < bindings.length; i++) {
            // System.out.println("BT " + bindings[i].getClass());
            mh = bindings[i].bindIntoOperation(mh);
        }

//        int count = bindings.length + (site.requiresBeanInstance() ? 1 : 0);
//
//        System.out.println(mh.type());
        // reduce (LifetimeObjectArena, *)X -> (LifetimeObjectArena)X
        if (mh.type().parameterCount() != 0) {
            MethodType mt = MethodType.methodType(methodHandle.type().returnType(), LifetimeObjectArena.class);
            mh = MethodHandles.permuteArguments(mh, mt, new int[mh.type().parameterCount()]);
        }

        // System.out.println(mh.type());
        return mh;
    }

    // Der hvor den er god, er jo hvis man gerne vil lave noget naar alle operationer er faerdige.
    // Fx freeze arrayet

    public final Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        forEachBinding(b -> {
            if (b instanceof ExtensionServiceBindingSetup s) {
                requireNonNull(s.extensionBean);
                result.add(s.extensionBean);
            } else if (b instanceof ServiceBindingSetup s) {
                result.add(s.entry.provider.operation.bean);
            }
        });
        return result;
    }

    // readOnly. Will not work if for example, resolving a binding
    public final void forEachBinding(Consumer<? super BindingSetup> binding) {
        for (BindingSetup bs : bindings) {
            if (bs instanceof OperationBindingSetup nested) {
                nested.providingOperation.forEachBinding(binding);
            }
            binding.accept(bs);
        }
    }

    public final MethodHandle generateMethodHandle() {
        MethodHandle mh = buildInvoker0();
        if (mh.type().parameterCount() != 1) {
            System.err.println(mh.type());
            throw new Error("Bean : " + bean.path() + ", operation : " + name);
        }
        if (mh.type().parameterType(0) != LifetimeObjectArena.class) {
            System.err.println(mh.type());
            throw new Error();
        }
        if (!mh.type().equals(invocationType.methodType)) {
            System.err.println("OperationType " + this.toString());
            System.err.println("Expected " + invocationType.methodType);
            System.err.println("Actual " + mh.type());
            throw new Error();
        }
        return mh;
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

    /** Whether or not the first argument to the method handle must be the bean instance. */
    private boolean requiresBeanInstance() {
        if (this instanceof MethodInvokeOperationSetup s) {
          return !Modifier.isStatic(s.method.getModifiers());
        } else if (this instanceof FieldAccessOperationSetup s) {
            return !Modifier.isStatic(s.field.getModifiers());
          } 
        return false;
    }

    /** {@return an operation handle for this operation.} */
    public final OperationHandle toHandle(IntrospectedBean iBean) {
        try {
            return (OperationHandle) MH_NEW_OPERATION_HANDLE.invokeExact(this, iBean);
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

    /** An operation that invokes an underlying {@link Method}. */
    public static final class ConstructorInvokeOperationSetup extends OperationSetup implements OperationSiteMirror.OfConstructorInvoke {

        /** The method to invoke. */
        private final Constructor<?> constructor;

        /**
         * @param operator
         * @param site
         */
        public ConstructorInvokeOperationSetup(ExtensionSetup operator, BeanSetup bean, Constructor<?> constructor, MethodHandle methodHandle) {
            super(operator, bean, OperationType.ofExecutable(constructor));
            this.methodHandle = methodHandle;
            this.constructor = requireNonNull(constructor);
            name = "constructor";
            mirrorSupplier = BeanFactoryMirror::new;
        }

        /** {@inheritDoc} */
        @Override
        public Constructor<?> constructor() {
            return constructor;
        }
    }

    /** An operation that can access an underlying {@link Field}. */
    public static final class FieldAccessOperationSetup extends OperationSetup implements OperationSiteMirror.OfFieldAccess {

        /** The way we access the field. */
        private final AccessMode accessMode;

        /** The field to access. */
        private final Field field;

        /**
         * @param operator
         * @param site
         */
        public FieldAccessOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, MethodHandle methodHandle, Field field,
                AccessMode accessMode) {
            super(operator, bean, operationType);
            this.methodHandle = methodHandle;
            this.field = requireNonNull(field);
            this.accessMode = requireNonNull(accessMode);
        }

        /** {@inheritDoc} */
        @Override
        public AccessMode accessMode() {
            return accessMode;
        }

        /** {@inheritDoc} */
        @Override
        public boolean allowGet() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean allowSet() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Field field() {
            return field;
        }
    }

    /** An operation that invokes an underlying {@link Constructor}. */
    public static final class FunctionOperationSetup extends OperationSetup implements OperationSiteMirror.OfFunctionCall {

        // Can read it from the method... no
        private final Class<?> functionalInterface;

        /**
         * @param operator
         * @param site
         */
        public FunctionOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, MethodHandle methodHandle,
                Class<?> functionalInterface) {
            super(operator, bean, operationType);
            this.methodHandle = methodHandle;
            this.functionalInterface = requireNonNull(functionalInterface);
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> functionalInterface() {
            return functionalInterface;
        }
    }

    public static final class LifetimePoolAccessOperationSetup extends OperationSetup implements OperationSiteMirror.OfLifetimePoolAccess {

        /**
         * @param operator
         * @param site
         */
        public LifetimePoolAccessOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, MethodHandle methodHandle) {
            super(operator, bean, operationType);
            this.methodHandle = methodHandle;
            name = "InstantAccess";
        }

        /** {@inheritDoc} */
        @Override
        public Optional<OperationMirror> origin() {
            return bean.mirror().factoryOperation();
        }
    }

    public static final class MethodHandleInvokeOperationSetup extends OperationSetup implements OperationSiteMirror.OfMethodHandleInvoke {

        /**
         * @param operator
         * @param site
         */
        public MethodHandleInvokeOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType operationType, MethodHandle methodHandle) {
            super(operator, bean, operationType);
            this.methodHandle = methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public MethodType methodType() {
            return type.toMethodType();
        }
    }

    /** An operation that invokes an underlying {@link Method}. */
    public static final class MethodInvokeOperationSetup extends OperationSetup implements OperationSiteMirror.OfMethodInvoke {

        /** The method to invoke. */
        private final Method method;

        /**
         * @param operator
         * @param site
         */
        public MethodInvokeOperationSetup(ExtensionSetup operator, BeanSetup bean, OperationType type, Method method, MethodHandle methodHandle) {
            super(operator, bean, type);
            this.methodHandle = methodHandle;
            this.method = requireNonNull(method);
        }
        // MH -> mirror - no gen
        // MH -> Gen - With caching (writethrough to whereever the bean cache it)
        // MH -> LazyGen - With caching

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return method;
        }
    }
}
