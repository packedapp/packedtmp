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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.repository.ApplicationLauncher;
import app.packed.application.repository.GuestLauncher;
import app.packed.application.repository.ManagedInstance;
import app.packed.container.Wirelet;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import internal.app.packed.lifecycle.lifetime.runtime.ApplicationLaunchContext;

public final class PackedApplicationLauncher<I, H extends ApplicationHandle<I, ?>> implements LauncherOrFuture<I, H>, ApplicationLauncher<I> {

    final H handle;
    final ConcurrentHashMap<String, ManagedInstance<I>> instances = new ConcurrentHashMap<>();

    final boolean isManaged;

    public PackedApplicationLauncher(boolean isManaged, H handle) {
        this.handle = requireNonNull(handle);
        this.isManaged = isManaged;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ManagedInstance<I>> instance(String name) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ManagedInstance<I>> instances() {
        return instances.values().stream();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAvailable() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void disableLaunch() {}

    /** {@inheritDoc} */
    @Override
    public GuestLauncher<I> launcher() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public I startGuest(Wirelet... wirelets) {
        I i = ApplicationLaunchContext.launch(handle, RunState.STARTING, wirelets);
        if (isManaged) {
            instances.put(UUID.randomUUID().toString(), new PackedManagedInstance<I>((ManagedLifecycle) i));
        }
        return i;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationHandle<?, ?> handle() {
        return handle;
    }

}