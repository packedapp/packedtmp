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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.bean.lifecycle.InitializeOperationConfiguration;
import app.packed.bean.lifecycle.InitializeOperationMirror;
import app.packed.bean.lifecycle.StartOperationConfiguration;
import app.packed.bean.lifecycle.StartOperationMirror;
import app.packed.bean.lifecycle.StopOperationConfiguration;
import app.packed.bean.lifecycle.StopOperationMirror;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.ForInitialize;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOnStartHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle;

/**
 * Access helper for bean lifecycle operation handles and related classes.
 */
public abstract class BeanLifecycleAccessHandler extends AccessHelper {

    private static final Supplier<BeanLifecycleAccessHandler> CONSTANT = StableValue.supplier(() -> init(BeanLifecycleAccessHandler.class, InitializeOperationMirror.class));

    public static BeanLifecycleAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Creates a new InitializeOperationMirror.
     *
     * @param handle the initialize handle
     * @return the mirror
     */
    public abstract InitializeOperationMirror newInitializeOperationMirror(ForInitialize handle);

    /**
     * Creates a new StartOperationMirror.
     *
     * @param handle the start handle
     * @return the mirror
     */
    public abstract StartOperationMirror newStartOperationMirror(LifecycleOnStartHandle handle);

    /**
     * Creates a new StopOperationMirror.
     *
     * @param handle the stop handle
     * @return the mirror
     */
    public abstract StopOperationMirror newStopOperationMirror(LifecycleOperationStopHandle handle);

    /**
     * Creates a new InitializeOperationConfiguration.
     *
     * @param handle the initialize handle
     * @return the configuration
     */
    public abstract InitializeOperationConfiguration newInitializeOperationConfiguration(ForInitialize handle);

    /**
     * Creates a new StartOperationConfiguration.
     *
     * @param handle the start handle
     * @return the configuration
     */
    public abstract StartOperationConfiguration newStartOperationConfiguration(LifecycleOnStartHandle handle);

    /**
     * Creates a new StopOperationConfiguration.
     *
     * @param handle the stop handle
     * @return the configuration
     */
    public abstract StopOperationConfiguration newStopOperationConfiguration(LifecycleOperationStopHandle handle);
}
