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
package sandbox.app.packed.application.registry.usage;

import static app.packed.component.SidehandleBinding.Kind.FROM_CONTEXT;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ManagedApplicationRuntime;
import app.packed.bean.Bean;
import app.packed.component.SidehandleBinding;
import app.packed.lifecycle.Inject;
import app.packed.lifecycle.RunState;
import app.packed.lifecycle.sandbox.StopOption;
import app.packed.operation.Op1;
import sandbox.app.packed.application.registry.ApplicationTemplate;

/**
 *
 */
public record SimpleManagedApplication(@SidehandleBinding(FROM_CONTEXT) ManagedApplicationRuntime lifecycle, long nanoss) implements ManagedApplicationRuntime {

    // Problemet er vi skal definere en Handle Class... Der har <I> = ManagedLifecycle
    // Men syntes ogsaa det er fint at folk skal lave en guest bean
    public static final ApplicationTemplate<GuestApplicationHandle2> MANAGED = ApplicationTemplate.of(
            Bean.<ManagedApplicationRuntime>of(new Op1<@SidehandleBinding(FROM_CONTEXT) ManagedApplicationRuntime, ManagedApplicationRuntime>(e -> e) {}), GuestApplicationHandle2.class,
            GuestApplicationHandle2::new);

    public static final ApplicationTemplate<GuestApplicationHandle> MANAGED_SUB_APPLICATION = ApplicationTemplate.of(Bean.of(SimpleManagedApplication.class),
            GuestApplicationHandle.class, GuestApplicationHandle::new);

    @Inject
    public SimpleManagedApplication(@SidehandleBinding(FROM_CONTEXT) ManagedApplicationRuntime lifecyle) {
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
    public void stop(StopOption... options) {
        lifecycle.stop(options);
    }

    @Override
    public CompletableFuture<Void> stopAsync(StopOption... options) {
        return lifecycle.stopAsync(options);
    }

    public static class GuestApplicationHandle extends ApplicationHandle<SimpleManagedApplication, ApplicationConfiguration> {

        /**
         * @param installer
         */
        GuestApplicationHandle(ApplicationInstaller<?> installer) {
            super(installer);
        }
    }

    public static class GuestApplicationHandle2 extends ApplicationHandle<ManagedApplicationRuntime, ApplicationConfiguration> {

        /**
         * @param installer
         */
        GuestApplicationHandle2(ApplicationInstaller<?> installer) {
            super(installer);
        }
    }

}
