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

import app.packed.base.Nullable;
import app.packed.guest.Guest;
import app.packed.guest.GuestState;

/**
 *
 */
public class PackedGuest implements Guest {

    // Hmm, maybe not
    @Nullable
    final PackedGuest parent;

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

    /** {@inheritDoc} */
    @Override
    public Guest stop(GuestStopOption... options) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> stopAsync(T result, GuestStopOption... options) {
        return null;
    }
}
