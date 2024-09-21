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

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationRepository;
import app.packed.application.ApplicationTemplate;

/** Implementation of {@link ApplicationRepository}. */
public final class PackedApplicationRepository<H extends ApplicationHandle<?, ?>> implements ApplicationRepository<H> {

    /** All applications that are installed in the container. */
    private final ConcurrentHashMap<String, H> handles;

    /** All application templates that are available when installing new applications at runtime. */
    public final Map<ApplicationTemplate<?>, MethodHandle> templates;

    @SuppressWarnings("unchecked")
    public PackedApplicationRepository(BuildApplicationRepository bar) {
        this.handles = new ConcurrentHashMap<>((Map<String, H>) bar.handles);
        this.templates = Map.copyOf(bar.ms);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<H> get(String name) {
        return Optional.ofNullable(handles.get(name));
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
    public Set<ApplicationTemplate<?>> templates() {
        return templates.keySet();
    }
}
