/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.concurrent;

import java.util.concurrent.ThreadFactory;

import app.packed.concurrent.JobConfiguration;
import app.packed.concurrent.ThreadKind;
import app.packed.operation.OperationInstaller;

/**
 *
 */
public class AbstractJobOperationHandle<T extends JobConfiguration> extends ThreadedOperationHandle<T> {

    public boolean interruptOnStop;

    public ThreadFactory threadFactory;

    public ThreadKind threadKind;

    protected AbstractJobOperationHandle(OperationInstaller installer) {
        super(installer);
    }

    @Override
    protected void onConfigured() {
        ThreadFactory tf = threadFactory;
        if (tf == null) {
            tf = threadKind.threadFactory();
        }
        sidehandle().bindConstant(ThreadFactory.class, tf);
    }
}
