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
package app.packed.concurrent.job2.impl;

import app.packed.bean.lifecycle.Initialize;
import app.packed.concurrent.oldscheduling.ScheduledOperationConfiguration;
import app.packed.concurrent.oldscheduling.ScheduledOperationMirror;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.concurrent.ScheduleImpl;
import internal.app.packed.concurrent.ThreadNamespaceHandle;
import internal.app.packed.concurrent.ThreadedOperationHandle;

/**
 *
 */
public final class ScheduledOperationHandle extends ThreadedOperationHandle<ScheduledOperationConfiguration> {

    /** A context template. */
    public static final OperationTemplate SCHEDULING_OPERATION_TEMPLATE = OperationTemplate.defaults().withReturnIgnore();

    @Nullable
    public ScheduleImpl s;

    /**
     * @param installer
     */
    public ScheduledOperationHandle(OperationInstaller installer, ThreadNamespaceHandle namespace) {
        super(installer, namespace);
    }

    @Override
    protected ScheduledOperationConfiguration newOperationConfiguration() {
        return new ScheduledOperationConfiguration(this);
    }

    @Override
    protected ScheduledOperationMirror newOperationMirror() {
        return new ScheduledOperationMirror(this);
    }

    @Override
    protected void onClose() {
        /// Called from a code generation thread
        if (s == null) {
            throw new IllegalStateException("Operation " + this + " was never scheduled");
        }
    }

    static class ScheduledOperationSideBean {

        @Initialize
        public void init() {}

    }
}
