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
package internal.app.packed.application.repository;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import app.packed.application.ApplicationHandle;
import app.packed.application.repository.ApplicationLauncher;
import app.packed.application.repository.GuestLauncher;
import app.packed.container.Wirelet;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;

/**
 *
 */
public sealed interface LauncherOrFuture<I, H extends ApplicationHandle<I, ?>> {

    record PackedApplicationLauncher<I, H extends ApplicationHandle<I, ?>>(AbstractApplicationRepository<I, H> repository, H handle)
            implements LauncherOrFuture<I, H>, ApplicationLauncher<I> {

        /** {@inheritDoc} */
        @Override
        public boolean isAvailable() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public void disable() {}

        /** {@inheritDoc} */
        @Override
        public GuestLauncher<I> launcher() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public I startGuest(Wirelet... wirelets) {
            I i = handle.launch(RunState.STARTING, wirelets);
            if (repository instanceof ManagedApplicationRepository<I,H> m) {
                m.instances.put(UUID.randomUUID().toString(), new PackedManagedInstance<I>((ManagedLifecycle) i));
            }
            return i;
        }

    }

    final class InstallingApplicationLauncher<I, H extends ApplicationHandle<I, ?>> implements LauncherOrFuture<I, H> {

        final CountDownLatch cdl = new CountDownLatch(1);

    }
}
