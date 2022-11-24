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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationSiteMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.IntrospectedBean;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.OperationSite.MethodOperationSite;
import internal.app.packed.operation.binding.BindingSetup;
import internal.app.packed.operation.binding.ExtensionServiceBindingSetup;
import internal.app.packed.operation.binding.OperationBindingSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Represents an operation on a bean. */
public class OperationSetup {

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

    /** Whether or not this operation can still be configured. */
    public boolean isClosed;

    /** Whether or not an invoker has been computed */
    private boolean isComputed;

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    /** The name of the operation */
    public String name; // name = operator.simpleName + "Operation"

    public ExtensionSetup operator;

    public PackedInvocationType pit = PackedInvocationType.DEFAULTS;

    /** The operation site. */
    public final OperationSite site;

    /** The type of this operation. */
    public final OperationType type;

    public OperationSetup(ExtensionSetup operator, OperationSite site) {
        this.operator = requireNonNull(operator);
        this.site = requireNonNull(site);
        this.type = site.type;
        this.bean = site.bean;
        this.bindings = site.type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[site.type.parameterCount()];
    }

    // Der hvor den er god, er jo hvis man gerne vil lave noget naar alle operationer er faerdige.
    // Fx freeze arrayet

    public final MethodHandle buildInvoker0() {
        site.bean.container.application.checkInCodegenPhase();

        if (isComputed) {
            // throw new IllegalStateException("This method can only be called once");
        }

        isComputed = true;

        MethodHandle mh = site.methodHandle;
        // System.out.println(mh.type() + " " + site);

//        System.out.println("--------Build Invoker-------------------");
//        System.out.println("Bean = " + bean.path() + ", operation = " + name + ", target = " + target.getClass().getSimpleName());
//        System.out.println(type);
//        System.out.println("Building Operation [bean = " + bean.path() + ": " + mh);

        if (bindings.length == 0) {
            if (!site.requiresBeanInstance()) {
                if (mh.type().parameterCount() > 0) {
//                   mh = MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
                    return mh;
                }
                return MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
            }
        }
        // mh = MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);

        if (site.requiresBeanInstance()) {
            mh = MethodHandles.collectArguments(mh, 0, site.bean.injectionManager.accessBean(site.bean));
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
            MethodType mt = MethodType.methodType(site.methodHandle.type().returnType(), LifetimeObjectArena.class);
            mh = MethodHandles.permuteArguments(mh, mt, new int[mh.type().parameterCount()]);
        }

        // System.out.println(mh.type());
        return mh;
    }

    public final Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        forEachBinding(b -> {
            if (b instanceof ExtensionServiceBindingSetup s) {
                requireNonNull(s.extensionBean);
                result.add(s.extensionBean);
            } else if (b instanceof ServiceBindingSetup s) {
                result.add(s.entry.provider.operation.site.bean);
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
            throw new Error("Bean : " + site.bean.path() + ", operation : " + name);
        }
        if (mh.type().parameterType(0) != LifetimeObjectArena.class) {
            System.err.println(mh.type());
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
    public static final class MethodOperationSetup extends OperationSetup implements OperationSiteMirror.OfMethodInvoke {

        /** The method to invoke. */
        private final Method method;

        /**
         * @param operator
         * @param site
         */
        public MethodOperationSetup(ExtensionSetup operator, BeanSetup bean, Method method, MethodHandle methodHandle) {
            super(operator, new MethodOperationSite(bean, methodHandle, method));
            this.method = requireNonNull(method);
        }

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return method;
        }
    }
}
