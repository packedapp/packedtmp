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
package internal.app.packed.lifetime.runtime;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.lifetime.RunState;
import app.packed.lifetime.StopOption;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.entrypoint.OldEntryPointSetup;
import sandbox.lifetime.external.LifecycleController;

/**
 *
 */
// Altsaa det fede var jo hvis vi kunne lave en generisk statemachine.
/// current state + Mask
/// Error bit (data =
// Desired state + Mask
// Extra data... Startup/Initialization exception
public final class PackedManagedLifetime implements LifecycleController {

    /**
     * A lock used for lifecycle control of the component. If components are arranged in a hierarchy and multiple components
     * need to be locked. A child component must lock itself before locking the parent.
     */
    final ReentrantLock lock = new ReentrantLock();

    /** A condition used for waiting on state changes from {@link #await(RunState, long, TimeUnit)}. */
    final Condition lockAwaitState = lock.newCondition();

    // Taenker vi maaske gaar tilbage til det design hvor vi havde en abstract state klasse... med
    // en implementering per state... Er jo mest brugbart i forbindelse med start/stop hvor vi har noget
    // midlertidigt state,paa den anden side kan vi maaske have lidt mindre state?
    volatile RunState state = RunState.UNINITIALIZED;

    volatile Throwable throwable;

    final ContainerRunner runner;

    public PackedManagedLifetime(ContainerRunner runner) {
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

    void initialize(ContainerSetup container, ContainerRunner cr) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.state = RunState.INITIALIZING;
        } finally {
            lock.unlock();
        }
        try {
            cr.initialize(container);
        } catch (Throwable t) {
            this.state = RunState.TERMINATED;
            this.throwable = t;
            throw t;
        }
        lock.lock();
        try {
            this.state = RunState.INITIALIZED;
        } finally {
            lock.unlock();
        }
    }

    void start(ContainerSetup container, ContainerRunner cr) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.state = RunState.STARTING;
        } finally {
            lock.unlock();
        }
        try {
            cr.start(container);
        } catch (Throwable t) {
            this.state = RunState.TERMINATED;
            this.throwable = t;
            throw t;
        }
        lock.lock();
        try {
            this.state = RunState.RUNNING;
        } finally {
            lock.unlock();
        }
    }

    void shutdown(ContainerSetup container, ContainerRunner cr) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.state = RunState.STOPPING;
        } finally {
            lock.unlock();
        }
        try {
            cr.shutdown(container);
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

    public void launch(ContainerSetup container, ContainerRunner cr) {
        initialize(container, cr); // may throw

        start(container, cr);

        OldEntryPointSetup ep = container.lifetime.entryPoints.entryPoint;

        if (ep != null) {
            ep.enter(cr);
        }

        shutdown(container, cr);

    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (state == RunState.UNINITIALIZED) {
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
    public RunState currentState() {
        return state;
    }

    /** {@inheritDoc} */
    @Override
    public void stop(StopOption... options) {

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