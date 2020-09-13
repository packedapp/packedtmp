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

import app.packed.base.Nullable;
import app.packed.guest.Guest;
import app.packed.guest.GuestState;

/**
 *
 */

// Altsaa det fede var jo hvis vi kunne lave en generisk statemachine.

/// current state + Mask
/// Error bit (data = 
// Desired state + Mask
//

// Extra data... Startup/Initialization exception

public class PackedGuest implements Guest {
    private final Sync sync = new Sync();

    static final int I_INITIALIZING = GuestState.INITIALIZING.ordinal();
    static final int I_INITIALIZED = GuestState.INITIALIZED.ordinal();
    static final int I_STARTING = GuestState.STARTING.ordinal();
    static final int I_RUNNING = GuestState.RUNNING.ordinal();
    static final int I_STOPPING = GuestState.STOPPING.ordinal();
    static final int I_TERMINATED = GuestState.TERMINATED.ordinal();

    // Hmm, maybe not
    @Nullable
    final PackedGuest parent;

    volatile Object data;

    PackedGuest(@Nullable PackedGuest parent) {
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public Guest start() {
        // System.out.println("START");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> startAsync(T result) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GuestState state() {
        throw new UnsupportedOperationException();

    }

    static void initializeAndStart(ComponentNodeConfiguration component, PackedInitializationContext pic) {
        // initialize
        new ComponentNode(null, component, pic);

        // TODO should check guest.delayStart wirelet
        pic.guest().start();
    }

    /** {@inheritDoc} */
    @Override
    public Guest stop(StopOption... options) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void await(GuestState state) throws InterruptedException {}

    /** {@inheritDoc} */
    @Override
    public boolean await(GuestState state, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @SuppressWarnings("serial") // Guest is not synchronized
    public static final class Sync extends AbstractQueuedSynchronizer {

    }
}
