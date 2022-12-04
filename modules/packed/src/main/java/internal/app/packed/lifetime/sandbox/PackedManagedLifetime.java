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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.lifetime.RunState;
import app.packed.lifetime.sandbox.LifetimeState;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import app.packed.lifetime.sandbox.StopOption;
import internal.app.packed.application.ApplicationInitializationContext;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.EntryPointSetup;

/**
 *
 */
// Altsaa det fede var jo hvis vi kunne lave en generisk statemachine.
/// current state + Mask
/// Error bit (data = 
// Desired state + Mask
// Extra data... Startup/Initialization exception
public final class PackedManagedLifetime implements ManagedLifetimeController {

    // Sagtens encode det i sync ogsaa
    RunState desiredState = RunState.UNINITIALIZED;

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

    public PackedManagedLifetime(ApplicationInitializationContext launchContext) {}

    // Hmm, maybe not
//    @Nullable
//    final PackedContainer parent;
    /** {@inheritDoc} */
    @Override
    public void await(RunState state) throws InterruptedException {
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
    public boolean await(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
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
    public LifetimeState info() {
        return null;
    }

    public void launch(ApplicationSetup application, ApplicationInitializationContext launchContext) {
        boolean isMain = false;
        if (application.entryPoints != null) {
            isMain = application.entryPoints.hasMain();
        }
        boolean start = isMain;
        final ReentrantLock lock = this.lock;

        lock.lock();
        try {
            if (!start) {
                this.state = RunState.INITIALIZED;
                this.desiredState = RunState.INITIALIZED;
                return;
            } else {
                this.state = RunState.STARTING;
                this.desiredState = RunState.RUNNING;
            }
        } finally {
            lock.unlock();
        }

        lock.lock();
        try {
            this.state = RunState.RUNNING;
            this.desiredState = RunState.RUNNING;
            if (!isMain) {
                return;
            }
        } finally {
            lock.unlock();
        }

        EntryPointSetup ep = application.entryPoints;
        if (ep != null) {
            ep.enter(launchContext);
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
    public RunState state() {
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
    public <T> CompletionStage<T> whenAt(RunState state, T object) {
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
}
//
//static final int I_INITIALIZING = ContainerState.INITIALIZING.ordinal();
//static final int I_INITIALIZED = ContainerState.INITIALIZED.ordinal();
//static final int I_STARTING = ContainerState.STARTING.ordinal();
//static final int I_RUNNING = ContainerState.RUNNING.ordinal();
//static final int I_STOPPING = ContainerState.STOPPING.ordinal();
//static final int I_TERMINATED = ContainerState.TERMINATED.ordinal();