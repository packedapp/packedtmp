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
package internal.app.packed.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import app.packed.binding.ProvisionException;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.binding.BindingProvider.FromCodeGeneratedConstant;
import internal.app.packed.binding.BindingProvider.FromConstant;
import internal.app.packed.binding.BindingProvider.FromInvocationArgument;
import internal.app.packed.binding.BindingProvider.FromLifetimeArena;
import internal.app.packed.binding.BindingProvider.FromOperationResult;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.invoke.MethodHandleUtil.LazyResolvable;
import internal.app.packed.operation.OperationMemberTarget.OperationConstructorTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;
import internal.app.packed.util.types.ClassUtil;

/**
 *
 */
public final class OperationCodeGenerator {

    /** The generated method handle. */
    // Vi har behov for cachen fordi kode generering kalde generate rekursivt.
    @Nullable
    private MethodHandle cachedMethodHandle;

    @Nullable
    public LazyResolvable cachedLazyMethodHandle;

    /** The operation we are generating a method handle for */
    private final OperationSetup operation;

    public OperationCodeGenerator(OperationSetup operation) {
        this.operation = requireNonNull(operation);
    }

    MethodHandle generate(boolean lazy) {
        if (lazy) {
            LazyResolvable lazyMH = cachedLazyMethodHandle;
            if (lazyMH == null) {
                lazyMH = cachedLazyMethodHandle = MethodHandleUtil.lazyF(operation.template.methodType, () -> newMethodHandle());
            }
            return lazyMH.handle();
        } else {
            return generateMethodHandle();
        }
    }

    /**
     * Creates an invoker as a method handle.
     * <p>
     * This method returns a method handle that can be used to invoke the underlying operation directly. The returned method
     * handle will have a type matching {@link #invokerType()}.
     *
     * @return a method handle for invoking the underlying operation
     */

    /**
     * Generates a method handle that can be used to invoke the underlying operation. *
     * <p>
     * This method should never be called directly, only through {@link #configuration}
     * <p>
     * This method will be never called more than once for a single operation.
     *
     * <p>
     * This method cannot be called earlier than the code generating phase of the application.
     * <p>
     * The {@link MethodType method type} of the returned method handle must be {@code invocationType()}.
     *
     * @return the generated method handle
     *
     * @throws IllegalStateException
     *             if called before the code generating phase of the application.
     */
    private MethodHandle generateMethodHandle() {
        // Maybe have a check here instead, and specifically mention generateMethodHandle when calling
        // Check only called once
        MethodHandle mh = cachedMethodHandle;
        if (mh == null) {
            mh = cachedMethodHandle = newMethodHandle();
        }
        return mh;
    }

    private MethodHandle newMethodHandle() {
        operation.bean.container.application.checkInCodegenPhase();
        ArrayList<Integer> permuters = new ArrayList<>();

        MethodHandle mh = operation.target.methodHandle();
        // debug("%s: %s -> %s", operation.bean.path(), initial.type(), operation.template.invocationType());

        // instance fields and methods, needs a bean instance as the first parameter
        boolean requiresBeanInstance = operation.target instanceof MemberOperationTarget mot && !(mot.target instanceof OperationConstructorTarget)
                && !Modifier.isStatic(mot.target.modifiers());
        if (requiresBeanInstance) {
            BindingProvider beanInstanceProvider = operation.bean.beanInstanceBindingProvider();
            mh = provide(mh, beanInstanceProvider, permuters);
        }

        for (BindingSetup binding : operation.bindings) {
            if (binding.provider() == null) {
                IO.println(operation.type);
                // Should not really be here where we fail, much earlier
                throw new ProvisionException("Oops");
            }
            mh = provide(mh, binding.provider(), permuters);
        }

        int[] result = new int[permuters.size()];
        for (int i = 0; i < permuters.size(); i++) {
            result[i] = permuters.get(i);
        }
        MethodType mt = operation.template.invocationType();

        // Embedded operations normally needs to return a value
        if (operation.embeddedInto != null) {
            mt = mt.changeReturnType(operation.type.returnRawType());
        }

        try {
            mh = MethodHandles.permuteArguments(mh, mt, result);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Permuter array " + permuters);
            result[1] = 1;
            mh = MethodHandles.permuteArguments(mh, mt, result);
//            throw e;
        }
        if (mh.type() != mt) {
            System.err.println("OperationType " + operation.type.toString());
            System.err.println("Expected " + operation.template.methodType);
            System.err.println("Actual " + mh.type());
            throw new Error();
        }
        assert mh.type() == operation.template.invocationType();
        return mh;
    }

    private MethodHandle provide(MethodHandle mh, BindingProvider p, ArrayList<Integer> permuters) {
        if (p instanceof FromConstant c) {
            return MethodHandles.insertArguments(mh, permuters.size(), c.constant());
        } else if (p instanceof FromCodeGeneratedConstant g) {
            Object constant = g.supplier().get();
            // TODO validate type
            return MethodHandles.insertArguments(mh, permuters.size(), constant);
        } else if (p instanceof FromInvocationArgument c) {
            permuters.add(c.argumentIndex());
            return mh;
        } else if (p instanceof FromOperationResult fo) {
            MethodHandle methodHandle = fo.operation().codeHolder.generateMethodHandle();

            mh = MethodHandles.collectArguments(mh, permuters.size(), methodHandle);
            for (int j = 0; j < methodHandle.type().parameterCount(); j++) {
                permuters.add(j);
            }
            return mh;
        } else if (p instanceof FromLifetimeArena fla) {
            permuters.add(0); // ExtensionContext is always 0
            MethodHandle tmp = MethodHandles.insertArguments(ServiceHelper.MH_CONSTANT_POOL_READER, 1, fla.index().index);
            assert tmp.type().returnType() == Object.class;
//            IO.println("FLA ->" + fla.type());
//            IO.println(mh.type());
//
//            // (LifetimePool)Object -> (LifetimePool)type
//            IO.println("TMP Before " +tmp.type());

            // We need to convert it from Object to the expected type
            tmp = tmp.asType(tmp.type().changeReturnType(fla.type()));

            // if (tmp.type().parameterCount() == 1) {
//                if (tmp.type().returnType() == Hmm2.RAR.class) {
//                 //  tmp = tmp.asType(tmp.type().changeReturnType(Hmm2.AR.class));
//                }
//            }

            return MethodHandles.collectArguments(mh, 0, tmp);
        } else {
            IO.println(p.getClass());
            if (ClassUtil.isInnerOrLocal(operation.bean.bean.beanClass)) {
                System.err.println("Inner Bean");
            }
            throw new UnsupportedOperationException("" + p + " " + operation.target.target());
        }
    }
}
