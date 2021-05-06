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
package packed.internal.component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.application.ApplicationRuntime;
import app.packed.state.sandbox.InstanceState;
import app.packed.state.sandbox.RunStateInfo;
import packed.internal.application.ApplicationLaunchContext;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.ApplicationSetup.MainThreadOfControl;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Altsaa det fede var jo hvis vi kunne lave en generisk statemachine.
/// current state + Mask
/// Error bit (data = 
// Desired state + Mask
// Extra data... Startup/Initialization exception
public class PackedApplicationRuntime implements ApplicationRuntime {

    // Sagtens encode det i sync ogsaa
    InstanceState desiredState = InstanceState.INITIALIZING;

    /**
     * A lock used for lifecycle control of the component. If components are arranged in a hierarchy and multiple components
     * need to be locked. A child component must lock itself before locking the parent.
     */
    final ReentrantLock lock = new ReentrantLock();

    /** A condition used for waiting on state changes from {@link #await(InstanceState, long, TimeUnit)}. */
    final Condition lockAwaitState = lock.newCondition();

    volatile InstanceState state = InstanceState.INITIALIZING;

    // Staten er selvf gemt i sync
    final Sync sync = new Sync();

    public PackedApplicationRuntime(ApplicationLaunchContext launchContext) {}

    // Hmm, maybe not
//    @Nullable
//    final PackedContainer parent;
    /** {@inheritDoc} */
    @Override
    public void await(InstanceState state) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (;;) {
                if (state.ordinal() <= state().ordinal()) {
                    return;
                }
                lockAwaitState.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean await(InstanceState state, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (;;) {
                if (state.ordinal() <= state().ordinal()) {
                    return true;
                } else if (nanos <= 0) {
                    return false;
                }
                nanos = lockAwaitState.awaitNanos(nanos);
            }
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public RunStateInfo info() {
        return null;
    }

    public void launch(ApplicationSetup application, ApplicationLaunchContext launchContext) {
        boolean isMain = application.hasMain();
        boolean start = isMain;
        final ReentrantLock lock = this.lock;

        lock.lock();
        try {
            if (!start) {
                this.state = InstanceState.INITIALIZED;
                this.desiredState = InstanceState.INITIALIZED;
                return;
            } else {
                this.state = InstanceState.STARTING;
                this.desiredState = InstanceState.RUNNING;
            }
        } finally {
            lock.unlock();
        }

        // run starting

        lock.lock();
        try {
            this.state = InstanceState.RUNNING;
            this.desiredState = InstanceState.RUNNING;
            if (!isMain) {
                return;
            }
        } finally {
            lock.unlock();
        }

        if (application.hasMain()) {
            MainThreadOfControl l = application.mainThread();
            if (!l.hasExecutionBlock()) {
                return; // runnint as deamon
            }

            try {
                if (l.cs.source.poolIndex > -1 && !l.isStatic) {
                    Object o = launchContext.pool().read(l.cs.source.poolIndex);
                    l.methodHandle.invoke(o);
                } else {
                    l.methodHandle.invoke();
                }

            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
        // todo run execution block

        // shutdown

    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (state == InstanceState.INITIALIZING) {
                throw new IllegalStateException("Cannot call this method now");
            }
            throw new UnsupportedOperationException();
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> startAsync(T result) {
        return CompletableFuture.supplyAsync(() -> {
            start();
            return result;
        });
    }

    /** {@inheritDoc} */
    @Override
    public InstanceState state() {
        return state;
    }

    /** {@inheritDoc} */
    @Override
    public void stop(StopOption... options) {}

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
        return null;
    }

    // Tag T istedet for container...
    public <T> CompletionStage<T> whenAt(InstanceState state, T object) {
        if (state().ordinal() >= state.ordinal()) {
            return CompletableFuture.completedFuture(object);
        }
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            throw new UnsupportedOperationException();
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("serial") // Guest is not synchronized
    public static final class Sync extends AbstractQueuedSynchronizer {

        @Override
        protected int tryAcquireShared(int arg) {
            return super.tryAcquireShared(arg);
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            return super.tryReleaseShared(arg);
        }
    }
}
//
//static final int I_INITIALIZING = ContainerState.INITIALIZING.ordinal();
//static final int I_INITIALIZED = ContainerState.INITIALIZED.ordinal();
//static final int I_STARTING = ContainerState.STARTING.ordinal();
//static final int I_RUNNING = ContainerState.RUNNING.ordinal();
//static final int I_STOPPING = ContainerState.STOPPING.ordinal();
//static final int I_TERMINATED = ContainerState.TERMINATED.ordinal();