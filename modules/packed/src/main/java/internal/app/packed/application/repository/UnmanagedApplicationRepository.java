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

import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.registry.other.ManagedInstance;

/**
 *
 */
public final class UnmanagedApplicationRepository<I, H extends ApplicationHandle<I, ?>> extends AbstractApplicationRepository<I, H> {

    /**
     * @param launchers
     * @param methodHandle
     * @param template
     */
    public UnmanagedApplicationRepository(BuildApplicationRepository bar) {
        super(bar);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ManagedInstance<I>> allManagedInstances() {
        throw new UnsupportedOperationException("This repository does not track unamanged application instance");
    }

}
