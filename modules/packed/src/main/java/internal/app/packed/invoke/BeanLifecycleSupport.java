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

import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.sidebean.SideBeanInstance;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.lifecycle.LifecycleOperationHandle;
import internal.app.packed.lifecycle.SomeLifecycleOperationHandle;
import internal.app.packed.lifecycle.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public class BeanLifecycleSupport {

    /** A MethodHandle for invoking invokeInitializer. */
    static final MethodHandle MH_INVOKE_INITIALIZER = LookupUtil.findStaticSelf(MethodHandles.lookup(), "invokeFactory", void.class, BeanSetup.class,
            MethodHandle.class, ExtensionContext.class);

    static final MethodHandle MH_INVOKE_INITIALIZER_SIDEBEAN = LookupUtil.findStaticSelf(MethodHandles.lookup(), "invokeFactory", void.class,
            SideBeanInstance.class, MethodHandle.class, ExtensionContext.class);

    public static void addLifecycleHandle(SomeLifecycleOperationHandle<LifecycleOperationHandle> handle) {
        OperationSetup os = OperationSetup.crack(handle.handle);


        // (ExtensionContext)Object
        os.bean.container.application.addCodegenAction(() -> {
            handle.methodHandle = handle.codeHolder.generate(false);
        });
    }

    static void invokeFactory(BeanSetup bean, MethodHandle mh, ExtensionContext ec) {
        Object instance;
        try {
            instance = mh.invokeExact(ec);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        if (instance == null) {
            throw new NullPointerException(" returned null");
        }

        // Move to higher up
        if (!bean.bean.beanClass.isInstance(instance)) {
            throw new Error("Expected " + bean.bean.beanClass + ", was " + instance.getClass());
        }

        // Store the new bean in the context
        PackedExtensionContext pec = (PackedExtensionContext) ec;
        pec.storeObject(bean.lifetimeStoreIndex, instance);
    }

    static void invokeFactory(SideBeanInstance sidebeanUsage, MethodHandle mh, ExtensionContext ec) {
        Object instance;
        try {
            instance = mh.invokeExact(ec);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        if (instance == null) {
            throw new NullPointerException(" returned null");
        }

        // Move to higher up
        if (!sidebeanUsage.bean.bean.beanClass.isInstance(instance)) {
            throw new Error("Expected " + sidebeanUsage.bean.bean.beanClass + ", was " + instance.getClass());
        }

        // Store the new bean in the context
        PackedExtensionContext pec = (PackedExtensionContext) ec;
        pec.storeObject(sidebeanUsage.lifetimeStoreIndex, instance);
    }
}
