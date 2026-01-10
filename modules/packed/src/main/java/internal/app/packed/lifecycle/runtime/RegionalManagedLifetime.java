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
package internal.app.packed.lifecycle.runtime;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.lifecycle.RunState;
import app.packed.lifecycle.runtime.ManagedLifecycle;
import app.packed.lifecycle.runtime.StopOption;
import app.packed.lifecycle.runtime.errorhandling.UnhandledApplicationException;
import internal.app.packed.lifecycle.lifetime.entrypoint.OldEntryPointSetup;

/**
 *
 */
// Altsaa det fede var jo hvis vi kunne lave en generisk statemachine.
/// current state + Mask
/// Error bit (data =
// Desired state + Mask
// Extra data... Startup/Initialization exception
public final class RegionalManagedLifetime implements ManagedLifecycle {

    /**
     * A lock used for lifecycle control of the component. If components are arranged in a hierarchy and multiple components
     * need to be locked. A child component must lock itself before locking the parent.
     */
    final ReentrantLock lock = new ReentrantLock();

    /** A condition used for waiting on state changes from {@link #await(RunState, long, TimeUnit)}. */
    final Condition lockAwaitState = lock.newCondition();

    final ContainerRunner runner;

    // Taenker vi maaske gaar tilbage til det design hvor vi havde en abstract state klasse... med
    // en implementering per state... Er jo mest brugbart i forbindelse med start/stop hvor vi har noget
    // midlertidigt state,paa den anden side kan vi maaske have lidt mindre state?
    volatile RunState state = RunState.UNINITIALIZED;

    volatile Thread thread;

    volatile Throwable throwable;

    public RegionalManagedLifetime(ContainerRunner runner) {
        this.runner = requireNonNull(runner);
    }

    /** {@inheritDoc} */
    @Override
    public void await(RunState state) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (;;) {
                if (state.ordinal() <= currentState().ordinal()) {
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
    public boolean await(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (;;) {
                if (state.ordinal() <= currentState().ordinal()) {
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
    public RunState currentState() {
        return state;
    }

    private void doInitialize() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            assert (state == RunState.UNINITIALIZED);
            this.state = RunState.INITIALIZING;
        } finally {
            lock.unlock();
        }

        try {
            runner.container.lifetime.initialize(runner.pool());
        } catch (Throwable t) {
            this.throwable = t;
            lock.lock();
            try {
                this.state = RunState.TERMINATED;
            } finally {
                lock.unlock();
            }
            // We never run any stop methods when an initialization method fails
            throw new UnhandledApplicationException(state, null, t);
            // throw t;
        }

        lock.lock();
        try {
            this.state = RunState.INITIALIZED;
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFailed() {
        return throwable != null;
    }

    public void launch(RunState desiredState) {
        if (desiredState == RunState.UNINITIALIZED) {
            throw new IllegalArgumentException("UNINITIALIZED is not a valid launch state");
        } else if (desiredState == RunState.INITIALIZING) {
            throw new IllegalArgumentException("INITIALIZING is not a valid launch state");
        }

        if (!runner.container.template.isManaged() && desiredState != RunState.INITIALIZED) {
            throw new IllegalArgumentException("Unmanaged applications and containers can only launch with runstate INITIALIZED, was " + desiredState);
        }

        doInitialize();

        if (desiredState == RunState.INITIALIZED) {
            return;
        }

        if (desiredState == RunState.STARTING) {
            startAsync();
            return;
        }

        start();

        if (desiredState == RunState.RUNNING) {
            return;
        }

        // Await only daemon threads
        //

//        try {
//            await(state);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        shutdown();
    }

    void shutdown() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.state = RunState.STOPPING;
        } finally {
            lock.unlock();
        }
        try {
            runner.shutdown();
        } catch (Throwable t) {
            this.state = RunState.TERMINATED;
            this.throwable = t;
            throw t;
        }
        lock.lock();
        try {
            this.state = RunState.TERMINATED;
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (;;) {
                if (state == RunState.UNINITIALIZED) {
                    throw new IllegalStateException("Cannot call this method now");
                } else if (state == RunState.INITIALIZED) {
                    this.state = RunState.STARTING;
                    break;
                } else if (state == RunState.STARTING) {
                    try {
                        await(RunState.RUNNING);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                } else {
                    return;
                }

            }
        } finally {
            lock.unlock();
        }
        start0();
    }

    void start0() {
        try {

            runner.start();
        } catch (Throwable t) {
            this.throwable = t;
            lock.lock();
            try {
                this.state = RunState.TERMINATED;
            } finally {
                lock.unlock();
            }
            throw t;
        }

        lock.lock();
        try {
            this.state = RunState.RUNNING;
        } finally {
            lock.unlock();
        }

        // Boer vel spawnes????
        OldEntryPointSetup ep = runner.container.lifetime.entryPoints.entryPoint;

        if (ep != null) {
            ep.enter(runner);
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

    void startFailed(Throwable t) {
        this.throwable = t;
        lock.lock();
        try {
            this.state = RunState.STOPPING;
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stop(StopOption... options) {
        shutdown();
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
        return null;
    }

    // Tag T istedet for container...
    public <T> CompletionStage<T> whenAt(RunState state, T object) {
        if (currentState().ordinal() >= state.ordinal()) {
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
}
//
//static final int I_INITIALIZING = ContainerState.INITIALIZING.ordinal();
//static final int I_INITIALIZED = ContainerState.INITIALIZED.ordinal();
//static final int I_STARTING = ContainerState.STARTING.ordinal();
//static final int I_RUNNING = ContainerState.RUNNING.ordinal();
//static final int I_STOPPING = ContainerState.STOPPING.ordinal();
//static final int I_TERMINATED = ContainerState.TERMINATED.ordinal();