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
package app.packed.application.registry.usage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.bean.lifecycle.Factory;
import app.packed.component.guest.GuestBinding;
import app.packed.operation.Op1;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;
import app.packed.util.Nullable;
import sandbox.lifetime.external.ManagedLifetimeState;

/**
 *
 */
public record SimpleManagedApplication(@GuestBinding ManagedLifecycle lifecycle, long nanos) implements ManagedLifecycle {

    // Problemet er vi skal definere en Handle Class... Der har <I> = ManagedLifecycle
    // Men syntes ogsaa det er fint at folk skal lave en guest bean
    public static final ApplicationTemplate<GuestApplicationHandle2> MANAGED = ApplicationTemplate
            .builder(new Op1<@GuestBinding ManagedLifecycle, ManagedLifecycle>(e -> e) {}).build(GuestApplicationHandle2.class, GuestApplicationHandle2::new);

    public static final ApplicationTemplate<GuestApplicationHandle> MANAGED_SUB_APPLICATION = ApplicationTemplate.builder(SimpleManagedApplication.class)
            .build(GuestApplicationHandle.class, GuestApplicationHandle::new);

    @Factory
    public SimpleManagedApplication(@GuestBinding ManagedLifecycle lifecyle) {
        this(lifecyle, System.nanoTime());
    }

    @Override
    public void await(RunState state) throws InterruptedException {
        lifecycle.await(state);
    }

    @Override
    public boolean await(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
        return lifecycle.await(state, timeout, unit);
    }

    @Override
    public RunState currentState() {
        return lifecycle.currentState();
    }

    @Override
    public boolean isFailed() {
        return lifecycle.isFailed();
    }

    @Override
    public void start() {
        lifecycle.start();
    }

    @Override
    public CompletableFuture<Void> startAsync() {
        return lifecycle.startAsync();
    }

    @Override
    public <T> CompletableFuture<@Nullable T> startAsync(@Nullable T result) {
        return lifecycle.startAsync(result);
    }

    @Override
    public ManagedLifetimeState state() {
        return lifecycle.state();
    }

    @Override
    public void stop(StopOption... options) {
        lifecycle.stop(options);
    }

    @Override
    public CompletableFuture<Void> stopAsync(StopOption... options) {
        return lifecycle.stopAsync(options);
    }

    @Override
    public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
        return lifecycle.stopAsync(result, options);
    }

    public static class GuestApplicationHandle extends ApplicationHandle<SimpleManagedApplication, ApplicationConfiguration> {

        /**
         * @param installer
         */
        GuestApplicationHandle(ApplicationInstaller<?> installer) {
            super(installer);
        }
    }

    public static class GuestApplicationHandle2 extends ApplicationHandle<ManagedLifecycle, ApplicationConfiguration> {

        /**
         * @param installer
         */
        GuestApplicationHandle2(ApplicationInstaller<?> installer) {
            super(installer);
        }
    }

}
