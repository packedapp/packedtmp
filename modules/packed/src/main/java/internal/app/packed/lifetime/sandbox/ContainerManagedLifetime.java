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
package internal.app.packed.lifetime.sandbox;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.ReentrantLock;

import internal.app.packed.util.LookupUtil;

/**
 *
 */
@SuppressWarnings("unused")
public class ContainerManagedLifetime {

    private final ReentrantLock lifetimeLock = new ReentrantLock();

    private static final VarHandle STATE = LookupUtil.findVarHandle(MethodHandles.lookup(), "state", int.class);

    // states: RUNNING -> SHUTDOWN -> TERMINATED
    private static final int UNINITIALIZED = 0;
    private static final int INITIALIZING = 1;
    private static final int INITIALIZED = 2;
    private static final int STARTING = 3;
    private static final int RUNNING = 4;
    private static final int SHUTDOWN = 5;
    private static final int TERMINATED = 6;

    private volatile int state;
    private volatile Throwable errorneous;
}
