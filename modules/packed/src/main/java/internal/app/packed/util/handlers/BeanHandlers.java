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
package internal.app.packed.util.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanMirror;
import app.packed.bean.scanning.BeanIntrospector;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanIntrospectorSetup;

/**
 *
 */
public final class BeanHandlers extends Handlers {

    /** A handle for invoking the protected method {@link BeanIntrospector#initialize()}. */
    private static final MethodHandle MH_BEAN_INTROSPECTOR_INITIALIZE = method(MethodHandles.lookup(), BeanIntrospector.class, "initialize", void.class,
            BeanIntrospectorSetup.class);

    /** A MethodHandle for invoking {@link BeanIntrospector#bean}. */
    private static final MethodHandle MH_BEAN_INTROSPECTOR_TO_SETUP = method(MethodHandles.lookup(), BeanIntrospector.class, "bean", BeanSetup.class);

    /** A handle that can access {@link BeanConfiguration#handle}. */
    private static final VarHandle VH_BEAN_CONFIGURATION_TO_HANDLE = field(MethodHandles.lookup(), BeanConfiguration.class, "handle", BeanHandle.class);

    /** A handle that can access {@link BeanHandleHandle#bean}. */
    private static final VarHandle VH_BEAN_HANDLE_TO_SETUP = field(MethodHandles.lookup(), BeanHandle.class, "bean", BeanSetup.class);

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_MIRROR_TO_HANDLE = field(MethodHandles.lookup(), BeanMirror.class, "handle", BeanHandle.class);

    public static BeanHandle<?> getBeanConfigurationHandle(BeanConfiguration configuration) {
        return (BeanHandle<?>) VH_BEAN_CONFIGURATION_TO_HANDLE.get(configuration);
    }

    public static BeanSetup getBeanHandleBean(BeanHandle<?> handle) {
        return (BeanSetup) VH_BEAN_HANDLE_TO_SETUP.get(handle);
    }

    public static BeanHandle<?> getBeanMirrorHandle(BeanMirror mirror) {
        return (BeanHandle<?>) VH_BEAN_MIRROR_TO_HANDLE.get(mirror);
    }

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_BEAN_HANDLE_DO_CLOSE = method(MethodHandles.lookup(), BeanHandle.class, "onStateChange", void.class, boolean.class);

    /** Call {@link BeanHandle#onClose()}. */
    public static void invokeBeanHandleDoClose(BeanHandle<?> handle, boolean isClose) {
        try {
            MH_BEAN_HANDLE_DO_CLOSE.invokeExact(handle, isClose);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    public static BeanSetup invokeBeanIntrospectorBean(BeanIntrospector<?> introspector) {
        try {
            return (BeanSetup) MH_BEAN_INTROSPECTOR_TO_SETUP.invokeExact(introspector);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    public static void invokeBeanIntrospectorInitialize(BeanIntrospector<?> introspector, BeanIntrospectorSetup ref) {
        try {
            MH_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, ref);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }
}
