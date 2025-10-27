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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import app.packed.bean.BeanSourceKind;
import app.packed.extension.ExtensionContext;
import app.packed.operation.OperationHandle;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.invoke.MethodHandleWrapper.ApplicationBaseLauncher;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.lifecycle.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationTarget.BeanAccessOperationTarget;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;

/**
 *
 */
public class ServiceHelper {

    public static ApplicationBaseLauncher fromGuestBeanHandle(GuestBeanHandle handle) {
        // Cleanup
        // return invokerfactory instead
        OperationHandle<?> oh = handle.lifecycleInvokers().get(0);

        MethodHandle m = OperationSetup.crack(oh).codeHolder.asMethodHandle();
        m = m.asType(m.type().changeReturnType(Object.class));
        return new ApplicationBaseLauncher(m);
    }

    public static MethodHandle fromOperation(OperationSetup o) {
        MethodHandle mh;

        LifetimeStoreIndex accessor = null;
        if (o.target instanceof BeanAccessOperationTarget) {
            accessor = o.bean.lifetimeStoreIndex;
            // test if prototype bean
            if (accessor == null && o.bean.bean.beanSourceKind != BeanSourceKind.INSTANCE) {
                o = o.bean.operations.first();
            }
        }
        if (!(o.target instanceof MemberOperationTarget) && o.bean.bean.beanSourceKind == BeanSourceKind.INSTANCE) {
            // It is a a constant
            mh = MethodHandles.constant(Object.class, o.bean.bean.beanSource);
            mh = MethodHandles.dropArguments(mh, 0, ExtensionContext.class);
        } else if (accessor != null) {
            mh = MethodHandles.insertArguments(PackedExtensionContext.MH_CONSTANT_POOL_READER, 1, accessor.index);
        } else {
            mh = o.codeHolder.newMethodHandle();
        }
        mh = mh.asType(mh.type().changeReturnType(Object.class));
        assert (mh.type().equals(MethodType.methodType(Object.class, ExtensionContext.class)));
        return mh;
    }
}
