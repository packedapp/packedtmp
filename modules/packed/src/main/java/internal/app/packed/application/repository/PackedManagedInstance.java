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
package internal.app.packed.application.repository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import app.packed.lifecycle.RunState;
import app.packed.lifecycle.runtime.ManagedLifecycle;
import app.packed.lifecycle.runtime.StopOption;
import app.packed.lifetimedynamic.ManagedInstance;
import org.jspecify.annotations.Nullable;
import sandbox.lifetime.external.ManagedLifetimeState;

/**
 *
 */
public record PackedManagedInstance<I>(ManagedLifecycle i) implements ManagedInstance<I> {

    @Override
    public void await(RunState state) throws InterruptedException {
        i.await(state);
    }

    @Override
    public boolean await(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
        return i.await(state, timeout, unit);
    }

    @Override
    public RunState currentState() {
        return i.currentState();
    }

    @Override
    public boolean isFailed() {
        return i.isFailed();
    }

    @Override
    public void start() {
        i.start();
    }

    @Override
    public CompletableFuture<Void> startAsync() {
        return i.startAsync();
    }

    @Override
    public <T> CompletableFuture<@Nullable T> startAsync(@Nullable T result) {
        return i.startAsync(result);
    }

    @Override
    public ManagedLifetimeState state() {
        return i.state();
    }

    @Override
    public void stop(StopOption... options) {
        i.stop(options);
    }

    @Override
    public CompletableFuture<Void> stopAsync(StopOption... options) {
        return i.stopAsync(options);
    }

    @Override
    public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
        return i.stopAsync(result, options);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<I> get() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public I getNow() {
        return (I) i;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return null;
    }

}
