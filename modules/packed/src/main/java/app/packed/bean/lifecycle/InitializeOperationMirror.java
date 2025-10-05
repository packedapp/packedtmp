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
package app.packed.bean.lifecycle;

import static java.util.Objects.requireNonNull;

import java.util.List;

import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.ForInitialize;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.BeanLifecycleAccessHandler;

/** A mirror representing an {@link OnInitialize} operation. */
public final class InitializeOperationMirror extends BeanLifecycleOperationMirror {

    /** A handle for the initialization operation. */
    final ForInitialize handle;

    /**
     * Create a new mirror.
     *
     * @param handle
     *            the operation's handle
     */
    InitializeOperationMirror(ForInitialize handle) {
        super(handle);
        this.handle = requireNonNull(handle);
    }

    // Maybe OperationMirrorList
    public List<InitializeOperationMirror> friends() {
        throw new UnsupportedOperationException();
    }
    // ListWithPointer into initialization methods on the bean

    static {
        AccessHelper.initHandler(BeanLifecycleAccessHandler.class, new BeanLifecycleAccessHandler() {

            @Override
            public InitializeOperationMirror newInitializeOperationMirror(ForInitialize handle) {
                return new InitializeOperationMirror(handle);
            }

            @Override
            public StartOperationMirror newStartOperationMirror(internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOnStartHandle handle) {
                return new StartOperationMirror(handle);
            }

            @Override
            public StopOperationMirror newStopOperationMirror(internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle handle) {
                return new StopOperationMirror(handle);
            }

            @Override
            public InitializeOperationConfiguration newInitializeOperationConfiguration(ForInitialize handle) {
                return new InitializeOperationConfiguration(handle);
            }

            @Override
            public StartOperationConfiguration newStartOperationConfiguration(internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOnStartHandle handle) {
                return new StartOperationConfiguration(handle);
            }

            @Override
            public StopOperationConfiguration newStopOperationConfiguration(internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle handle) {
                return new StopOperationConfiguration(handle);
            }
        });
    }
}
