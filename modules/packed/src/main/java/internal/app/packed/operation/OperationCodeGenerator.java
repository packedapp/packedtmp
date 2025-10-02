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
import java.util.ArrayList;

import app.packed.binding.ProvisionException;
import internal.app.packed.binding.BindingAccessor;
import internal.app.packed.binding.BindingAccessor.FromCodeGenerated;
import internal.app.packed.binding.BindingAccessor.FromConstant;
import internal.app.packed.binding.BindingAccessor.FromInvocationArgument;
import internal.app.packed.binding.BindingAccessor.FromLifetimeArena;
import internal.app.packed.binding.BindingAccessor.FromOperationResult;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.lifecycle.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;
import internal.app.packed.util.handlers.OperationHandlers;
import internal.app.packed.util.types.ClassUtil;

/**
 *
 */
public final class OperationCodeGenerator {

    private final ArrayList<Integer> permuters = new ArrayList<>();

    public static MethodHandle newMethodHandle(OperationSetup operation) {
        operation.bean.container.application.checkInCodegenPhase();
        return new OperationCodeGenerator().generate(operation, operation.target.methodHandle());
    }

    MethodHandle generate(OperationSetup operation, MethodHandle initial) {
        MethodHandle mh = initial;
        // debug("%s: %s -> %s", operation.bean.path(), initial.type(), operation.template.invocationType());

        // instance fields and methods, needs a bean instance
        boolean requiresBeanInstance = operation.target instanceof MemberOperationTarget s && s.needsBeanInstance();
        if (requiresBeanInstance) {
            BindingAccessor ba = operation.bean.beanInstanceBindingProvider();
            requireNonNull(ba);
            mh = provide(operation, mh, operation.bean.beanInstanceBindingProvider());
        }

        // debug(mh.type());
        for (BindingSetup binding : operation.bindings) {
//            IO.println(binding.resolver().getClass());
            // IO.println(mh.type());
            if (binding.resolver() == null) {
                IO.println(operation.type);
                // Should really be here where we fail, much earlier
                throw new ProvisionException("Oops");
            }
            mh = provide(operation, mh, binding.resolver());
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
        if (!mh.type().equals(mt)) {
            System.err.println("OperationType " + operation.type.toString());
            System.err.println("Expected " + operation.template.methodType);
            System.err.println("Actual " + mh.type());
            throw new Error();
        }
        return mh;
    }

    private MethodHandle provide(OperationSetup operation, MethodHandle mh, BindingAccessor p) {
        if (p instanceof FromConstant c) {
            return MethodHandles.insertArguments(mh, permuters.size(), c.constant());
        } else if (p instanceof FromCodeGenerated g) {
            Object constant = g.supplier().get();
            // TODO validate type
            return MethodHandles.insertArguments(mh, permuters.size(), constant);
        } else if (p instanceof FromInvocationArgument c) {
            permuters.add(c.argumentIndex());
            return mh;
        } else if (p instanceof FromOperationResult fo) {
            MethodHandle methodHandle = OperationHandlers.invokeOperationHandleNewMethodHandle(fo.operation().handle());

            mh = MethodHandles.collectArguments(mh, permuters.size(), methodHandle);
            for (int j = 0; j < methodHandle.type().parameterCount(); j++) {
                permuters.add(j);
            }
            return mh;
        } else if (p instanceof FromLifetimeArena fla) {
            permuters.add(0); // ExtensionContext is always 0
            MethodHandle tmp = MethodHandles.insertArguments(PackedExtensionContext.MH_CONSTANT_POOL_READER, 1, fla.index());
            assert (tmp.type().returnType() == Object.class);
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
//            IO.println("TMP After" +tmp.type());

//            IO.println(tmp.type());
//            IO.println(tmp.type().returnType());
//            IO.println();
//            IO.println(operation.type);
//            IO.println();
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
