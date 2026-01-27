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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationLauncher;
import app.packed.application.BootstrapApp;
import app.packed.binding.Key;
import app.packed.lifecycle.RunState;
import internal.app.packed.lifecycle.runtime.ApplicationLaunchContext;
import internal.app.packed.service.util.ServiceMap;

/**
 *
 */
public class PackedBootstrapLauncher<A> implements BootstrapApp.Launcher<A> {

    public String name;

    public List<String> args;

    public ServiceMap<Object> provided;

    private final ApplicationHandle<A, ?> applicationHandle;

    public PackedBootstrapLauncher(ApplicationHandle<A, ?> applicationHandle) {
        this.applicationHandle = requireNonNull(applicationHandle);
    }

    @Override
    public ApplicationLauncher args(String... args) {
        this.args = List.of(args);
        return this;
    }

    @Override
    public ApplicationLauncher named(String name) {
        this.name = requireNonNull(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> ApplicationLauncher provide(Key<? super T> key, T instance) {
        if (provided == null) {
            provided = new ServiceMap<>();
        }
        provided.put(key, instance);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public A launch(RunState state) {
        return ApplicationLaunchContext.launch(this, applicationHandle, state);
    }
}
