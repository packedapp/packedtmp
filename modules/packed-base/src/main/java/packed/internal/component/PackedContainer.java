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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.Image;
import app.packed.component.ShellDriver;
import app.packed.component.Wirelet;
import app.packed.container.Container;
import app.packed.container.ContainerState;

/**
 *
 */

// Altsaa det fede var jo hvis vi kunne lave en generisk statemachine.

/// current state + Mask
/// Error bit (data = 
// Desired state + Mask
//

// Extra data... Startup/Initialization exception

public class PackedContainer implements Container {

    final Sync sync = new Sync();

    /**
     * A lock used for lifecycle control of the component. If components are arranged in a hierarchy and multiple components
     * need to be locked. A child component must lock itself before locking the parent.
     */
    final ReentrantLock lock = new ReentrantLock();

    /** A condition used for waiting on state changes from {@link #await(ContainerState, long, TimeUnit)}. */
    final Condition lockAwaitState = lock.newCondition();

    volatile ContainerState state = ContainerState.INITIALIZING;

    ContainerState desiredState;

    public PackedContainer(PackedInitializationContext pic) {

    }

    // Hmm, maybe not
//    @Nullable
//    final PackedContainer parent;
    /** {@inheritDoc} */
    @Override
    public void await(ContainerState state) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (;;) {
                if (state.ordinal() <= getState().ordinal()) {
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
    public boolean await(ContainerState state, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (;;) {
                if (state.ordinal() <= getState().ordinal()) {
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

    public CompletionStage<Container> whenAt(ContainerState state) {
        if (getState().ordinal() >= state.ordinal()) {
            return CompletableFuture.completedFuture(this);
        }
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            throw new UnsupportedOperationException();
        } finally {
            lock.unlock();
        }
    }

    public ContainerState getState() {
        return state;
    }

    /** {@inheritDoc} */
    @Override
    public Container start() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
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
    public ContainerState state() {
        throw new UnsupportedOperationException();

    }

    static void start(ComponentBuild component, PackedInitializationContext pic) {

        // TODO should check guest.delayStart wirelet
        // pic.container().start();
    }

    /** {@inheritDoc} */
    @Override
    public Container stop(StopOption... options) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
        return null;
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

    /** An implementation of {@link Image} used by {@link ShellDriver#newImage(Assembly, Wirelet...)}. */
    public static final class ExecutingImage implements Image<Void> {

        /** The assembled image node. */
        private final ComponentBuild compConf;

        /**
         * Create a new image from the specified component.
         * 
         * @param compConf
         *            the assembled component
         */
        public ExecutingImage(ComponentBuild compConf) {
            this.compConf = requireNonNull(compConf);
        }

        /** {@inheritDoc} */
        @Override
        public Component component() {
            return compConf.adaptToComponent();
        }

        /** {@inheritDoc} */
        @Override
        public Void use(Wirelet... wirelets) {
            PackedInitializationContext.process(compConf, wirelets);
            return null;
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