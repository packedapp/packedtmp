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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

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

    static final int I_INITIALIZING = ContainerState.INITIALIZING.ordinal();
    static final int I_INITIALIZED = ContainerState.INITIALIZED.ordinal();
    static final int I_STARTING = ContainerState.STARTING.ordinal();
    static final int I_RUNNING = ContainerState.RUNNING.ordinal();
    static final int I_STOPPING = ContainerState.STOPPING.ordinal();
    static final int I_TERMINATED = ContainerState.TERMINATED.ordinal();

    // Hmm, maybe not
//    @Nullable
//    final PackedContainer parent;

    volatile Object data;

    /** {@inheritDoc} */
    @Override
    public Container start() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> startAsync(T result) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerState state() {
        throw new UnsupportedOperationException();

    }

    static void initializeAndStart(ComponentBuild component, PackedInitializationContext pic) {
        // initialize
        new PackedComponent(null, component, pic);

        // TODO should check guest.delayStart wirelet
        pic.container().start();
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

    /** {@inheritDoc} */
    @Override
    public void await(ContainerState state) throws InterruptedException {}

    /** {@inheritDoc} */
    @Override
    public boolean await(ContainerState state, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @SuppressWarnings("serial") // Guest is not synchronized
    public static final class Sync extends AbstractQueuedSynchronizer {

    }
}
