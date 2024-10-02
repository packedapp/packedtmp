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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationTemplate;
import app.packed.application.ApplicationTemplate.Installer;
import app.packed.application.repository.ApplicationRepository;
import app.packed.build.BuildGoal;
import internal.app.packed.ValueBased;

/** Implementation of {@link ApplicationRepository}. */
@ValueBased
public final class PackedApplicationRepository<H extends ApplicationHandle<?, ?>> implements ApplicationRepository<H> {

    /** All applications that are installed in the container. */
    private final ConcurrentHashMap<String, H> handles;

    /** MethodHandle to create the guest bean. */
    private final MethodHandle methodHandle;

    /** The template that is used to install new applications at runtime. */
    private final PackedApplicationTemplate<?, H> template;

    @SuppressWarnings("unchecked")
    public PackedApplicationRepository(BuildApplicationRepository bar) {
        this.handles = new ConcurrentHashMap<>((Map<String, H>) bar.handles);
        this.template = (PackedApplicationTemplate<?, H>) bar.template;
        this.methodHandle = requireNonNull(bar.mh);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<H> get(String name) {
        return Optional.ofNullable(handles.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public Installer<H> newApplication() {
        return template.newInstaller(BuildGoal.IMAGE, methodHandle);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return handles.size();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<H> stream() {
        return handles.values().stream();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationTemplate<?, H> template() {
        return template;
    }
}
