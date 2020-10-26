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
package packed.internal.inject.service.sandbox;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceLocator;
import packed.internal.inject.Dependant;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.inject.service.runtime.DelegatingRuntimeService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/** An entry specifically used for {@link ServiceExtension#provideAll(ServiceLocator)}. */
public final class RuntimeAdaptorServiceBuild extends ServiceBuild {

    /** The entry from the 'imported' injector. */
    private final RuntimeService entry;

    public RuntimeAdaptorServiceBuild(ConfigSite configSite, RuntimeService entry) {
        super(configSite, entry.key());
        this.entry = entry;
    }

    @Override
    @Nullable
    public Dependant dependant() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return entry.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        return new DelegatingRuntimeService(this, entry);
    }
}
