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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.IntrospectedBean;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.binding.BindingSetup;
import internal.app.packed.operation.binding.ExtensionServiceBindingSetup;
import internal.app.packed.operation.binding.OperationalBindingSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Represents an operation on a bean. */
public final class OperationSetup {

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

    /** By who and how this operation is invoked */
    public InvocationSite invocationSite;

    /** Whether or not an invoker has been computed */
    private boolean isComputed;

    /** Whether or not this operation can still be configured. */
    public boolean isConfigurationDisabled;

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    public ExtensionSetup operator;

    /** The name of the operation */
    public String name; // name = operator.simpleName + "Operation"

    /** The target of the operation. */
    public final OperationTarget target;

    /** The type of the operation. */
    public final OperationType type;

    public OperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator, OperationTarget operationTarget) {
        this.bean = requireNonNull(bean);
        this.type = requireNonNull(type);
        this.target = requireNonNull(operationTarget);
        this.operator = requireNonNull(operator);
        this.bindings = type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[type.parameterCount()];
    }

    // Der hvor den er god, er jo hvis man gerne vil lave noget naar alle operationer er faerdige.
    // Fx freeze arrayet

    public MethodHandle buildInvoker() {
        MethodHandle mh = buildInvoker0();
        if (mh.type().parameterCount() != 1) {
            System.err.println(mh.type());
            throw new Error("Bean : " + bean.path() + ", operation : " + name);
        }
        if (mh.type().parameterType(0) != LifetimeObjectArena.class) {
            System.err.println(mh.type());
            throw new Error();
        }
        return mh;
    }

    public MethodHandle buildInvoker0() {
        bean.container.application.checkInCodegenPhase();

        if (isComputed) {
         //   throw new IllegalStateException("This method can only be called once");
        }

        isComputed = true;

        MethodHandle mh = target.methodHandle;
//        System.out.println("--------Build Invoker-------------------");
//        System.out.println("Bean = " + bean.path() + ", operation = " + name + ", target = " + target.getClass().getSimpleName());
//        System.out.println(type);
//        System.out.println("Building Operation [bean = " + bean.path() + ": " + mh);

        //if (target instanceof BeanInstanceAccess)
        if (bindings.length == 0) {
            if (!target.requiresBeanInstance()) {
                if (mh.type().parameterCount() > 0) {
                    return mh;
                }
                return MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
            }
        }

        if (target.requiresBeanInstance()) {
            mh = MethodHandles.collectArguments(mh, 0, bean.injectionManager.accessBean(bean));
        }

        for (int i = 0; i < bindings.length; i++) {
        //    System.out.println("BT " + bindings[i].getClass());
            mh = bindings[i].bindIntoOperation(mh);
        }

        int count = bindings.length + (target.requiresBeanInstance() ? 1 : 0);

        // reduce (LifetimeObjectArena, *)X -> (LifetimeObjectArena)X
        if (count != 0) {
            MethodType mt = MethodType.methodType(target.methodHandle.type().returnType(), LifetimeObjectArena.class);
            mh = MethodHandles.permuteArguments(mh, mt, new int[count]);
        }

        requireNonNull(mh);
     //   System.out.println(mh.type());
        return mh;
        // Must be computed relative to invocating site
        // throw new UnsupportedOperationException();
    }

    // readOnly. Will not work if for example, resolving a binding
    public void forEachBinding(Consumer<? super BindingSetup> binding) {
        for (BindingSetup bs : bindings) {
            if (bs instanceof OperationalBindingSetup nested) {
                nested.providingOperation.forEachBinding(binding);
            }
            binding.accept(bs);
        }
    }

    /** {@return a new mirror.} */
    public OperationMirror mirror() {
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
    public OperationHandle toHandle(IntrospectedBean iBean) {
        try {
            return (OperationHandle) MH_NEW_OPERATION_HANDLE.invokeExact(this, iBean);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    public Set<BeanSetup> dependsOn() {
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

    /**
     * Extracts a bean setup from a bean handle.
     * 
     * @param handle
     *            the handle to extract from
     * @return the bean setup
     */
    public static OperationSetup crack(OperationHandle handle) {
        return (OperationSetup) VH_OPERATION_HANDLE_CRACK.get(handle);
    }
}
