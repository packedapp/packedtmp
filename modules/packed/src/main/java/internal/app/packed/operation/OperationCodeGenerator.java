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

import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromCodeGenerated;
import internal.app.packed.binding.BindingResolution.FromConstant;
import internal.app.packed.binding.BindingResolution.FromInvocationArgument;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.lifetime.runtime.PackedContainerContext;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;

/**
 *
 */
final class OperationCodeGenerator {

    private final ArrayList<Integer> permuters = new ArrayList<>();

    MethodHandle generate(OperationSetup operation, MethodHandle initial) {
        MethodHandle mh = initial;
      //  debug("%s %s", operation.bean.path(), initial.type());

        // instance fields and methods, needs a bean instance
        boolean requiresBeanInstance = operation instanceof MemberOperationSetup s && s.needsBeanInstance();
        if (requiresBeanInstance) {
            mh = provide(mh, operation.bean.beanInstanceBindingProvider());
        }

        for (BindingSetup binding : operation.bindings) {
            mh = provide(mh, binding.resolver());
        }

        int[] result = new int[permuters.size()];
        for (int i = 0; i < permuters.size(); i++) {
            result[i] = permuters.get(i);
        }
        MethodType mt = operation.template.invocationType();

        // Nested operations normally needs to return a value
        if (operation.embeddedInto != null) {
            mt = mt.changeReturnType(operation.type.returnRawType());
        }
        mh = MethodHandles.permuteArguments(mh, mt, result);

        if (!mh.type().equals(mt)) {
            System.err.println("OperationType " + operation.type.toString());
            System.err.println("Expected " + operation.template.methodType);
            System.err.println("Actual " + mh.type());
            throw new Error();
        }
        return mh;
    }

    private MethodHandle provide(MethodHandle mh, BindingResolution p) {
        requireNonNull(p);
        if (p instanceof FromConstant c) {
            return MethodHandles.insertArguments(mh, permuters.size(), c.constant());
        } else if (p instanceof FromCodeGenerated g) {
            Object constant = g.supplier().get();
            // TODO validate type
            return MethodHandles.insertArguments(mh, permuters.size(), constant);
        } else if (p instanceof FromInvocationArgument c) {
            permuters.add(c.argumentIndex());
            return mh;
        } else if (p instanceof FromOperation fo) {
            MethodHandle methodHandle = fo.operation().generateMethodHandle();
            mh = MethodHandles.collectArguments(mh, permuters.size(), methodHandle);
            for (int j = 0; j < methodHandle.type().parameterCount(); j++) {
                permuters.add(j);
            }
            return mh;
        } else if (p instanceof FromLifetimeArena fla) {
            permuters.add(0); // ExtensionContext is always 0
            MethodHandle tmp = MethodHandles.insertArguments(PackedContainerContext.MH_CONSTANT_POOL_READER, 1, fla.index());

            // (LifetimePool)Object -> (LifetimePool)type
            tmp = tmp.asType(tmp.type().changeReturnType(fla.type()));
            return MethodHandles.collectArguments(mh, 0, tmp);
        } else {
            throw new UnsupportedOperationException("" + p);
        }
    }
}
