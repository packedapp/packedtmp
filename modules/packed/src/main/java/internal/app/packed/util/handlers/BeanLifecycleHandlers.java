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

import app.packed.bean.lifecycle.InitializeOperationConfiguration;
import app.packed.bean.lifecycle.InitializeOperationMirror;
import app.packed.bean.lifecycle.StartOperationConfiguration;
import app.packed.bean.lifecycle.StartOperationMirror;
import app.packed.bean.lifecycle.StopOperationConfiguration;
import app.packed.bean.lifecycle.StopOperationMirror;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationInitializeHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStartHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle;

/**
 *
 */
public class BeanLifecycleHandlers extends Handlers {

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_INITIALIZATION_MIRROR = constructor(MethodHandles.lookup(), InitializeOperationMirror.class,
            LifecycleOperationInitializeHandle.class);

    public static InitializeOperationMirror newInitializeOperationMirror(LifecycleOperationInitializeHandle handle) {
        try {
            return (InitializeOperationMirror) MH_NEW_INITIALIZATION_MIRROR.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_START_MIRROR = constructor(MethodHandles.lookup(), StartOperationMirror.class,
            LifecycleOperationStartHandle.class);

    public static StartOperationMirror newStartOperationMirror(LifecycleOperationStartHandle handle) {
        try {
            return (StartOperationMirror) MH_NEW_START_MIRROR.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_STOP_MIRROR = constructor(MethodHandles.lookup(), StopOperationMirror.class, LifecycleOperationStopHandle.class);

    public static StopOperationMirror newStopOperationMirror(LifecycleOperationStopHandle handle) {
        try {
            return (StopOperationMirror) MH_NEW_STOP_MIRROR.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }




    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_INITIALIZATION_CONFIGURATION = constructor(MethodHandles.lookup(), InitializeOperationConfiguration.class,
            LifecycleOperationInitializeHandle.class);

    public static InitializeOperationConfiguration newInitializeOperationConfiguration(LifecycleOperationInitializeHandle handle) {
        try {
            return (InitializeOperationConfiguration) MH_NEW_INITIALIZATION_CONFIGURATION.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_START_CONFIGURATION = constructor(MethodHandles.lookup(), StartOperationConfiguration.class,
            LifecycleOperationStartHandle.class);

    public static StartOperationConfiguration newStartOperationConfiguration(LifecycleOperationStartHandle handle) {
        try {
            return (StartOperationConfiguration) MH_NEW_START_CONFIGURATION.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_STOP_CONFIGURATION = constructor(MethodHandles.lookup(), StopOperationConfiguration.class, LifecycleOperationStopHandle.class);

    public static StopOperationConfiguration newStopOperationConfiguration(LifecycleOperationStopHandle handle) {
        try {
            return (StopOperationConfiguration) MH_NEW_STOP_CONFIGURATION.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }
}
