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
import java.util.Map;

import app.packed.bean.BeanSourceKind;
import app.packed.operation.OperationHandle;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.invoke.MethodHandleInvoker.ApplicationBaseLauncher;
import internal.app.packed.invoke.MethodHandleInvoker.ExportedServiceWrapper;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.lifecycle.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationTarget.BeanAccessOperationTarget;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;
import internal.app.packed.util.CollectionUtil;

/**
 *
 */
public class ServiceHelper {

    /** A method handle for calling {@link PackedExtensionContext#read(int)} at runtime. */
    static final MethodHandle MH_CONSTANT_POOL_READER;

    static {
        MethodHandle m = LookupUtil.findVirtual(MethodHandles.lookup(), PackedExtensionContext.class, "read", Object.class, int.class);
        MethodType mt = m.type().changeParameterType(0, ExtensionContext.class);
        MH_CONSTANT_POOL_READER = m.asType(mt);
    }

    public static Map<String, MethodHandle> forTestingMap(Map<String, OperationHandle<?>> ink) {
        return CollectionUtil.copyOf(ink, v -> OperationSetup.crack(v).someHandle.codeHolder.generate(true));
    }

    public static ApplicationBaseLauncher newApplicationBaseLauncher(GuestBeanHandle handle) {
        MethodHandle m = OperationSetup.crack(handle.factory()).someHandle.codeHolder.generate(true);
        m = MethodHandles.explicitCastArguments(m, m.type().changeReturnType(Object.class));
        return new ApplicationBaseLauncher(m);
    }

    public static ExportedServiceWrapper toExportedService(OperationSetup o) {
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
            mh = MethodHandles.insertArguments(MH_CONSTANT_POOL_READER, 1, accessor.index);
        } else {
            mh = o.someHandle.codeHolder.generate(false);
        }
        mh = mh.asType(mh.type().changeReturnType(Object.class));
        assert mh.type() == MethodType.methodType(Object.class, ExtensionContext.class);
        return new ExportedServiceWrapper(mh);
    }
}
