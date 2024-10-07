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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.repository.ManagedInstance;
import app.packed.lifecycle.OnStop;
import app.packed.lifecycle.OnStopContext;
import app.packed.operation.OperationDependencyOrder;
import app.packed.runtime.RunState;
import app.packed.runtime.StopOption;

/**
 *
 */
public final class ManagedApplicationRepository<I, H extends ApplicationHandle<I, ?>> extends AbstractApplicationRepository<I, H> {

    final ConcurrentHashMap<String, ManagedInstance<I>> instances = new ConcurrentHashMap<>();

    volatile boolean isStopped;

    /**
     * @param launchers
     * @param methodHandle
     * @param template
     */
    public ManagedApplicationRepository(BuildApplicationRepository bar) {
        super(bar);
    }

    private void checkNotStopped() {}

    /** {@inheritDoc} */
    @Override
    public Optional<ManagedInstance<I>> instance(String name) {
        checkNotStopped();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ManagedInstance<I>> instances() {
        checkNotStopped();
        return instances.values().stream();
    }

    @OnStop(order = OperationDependencyOrder.AFTER_DEPENDENCIES)
    public void stopAfterDependencis(OnStopContext c) {
        for (ManagedInstance<I> i : instances.values()) {
            c.await((l, u) -> i.await(RunState.TERMINATED, l, u));
        }
    }

    @OnStop(order = OperationDependencyOrder.BEFORE_DEPENDENCIES)
    // Need to wait on any installs to finish
    public void stopBeforeDependencies() {
        isStopped = true;

        // Disable all existing launchers
        launchers().forEach(l -> l.disable());

        System.out.println(instances.size());
        // Propbably need to have some kind of limit if we have millions of applications.
        // But than again virtual virtual threads really scale. But micro apps
        instances().forEach(a -> {
            a.stop(StopOption.parentStopping());
        });
    }
}