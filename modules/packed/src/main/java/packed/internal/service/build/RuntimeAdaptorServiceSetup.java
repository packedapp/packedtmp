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
package packed.internal.service.build;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceLocator;
import packed.internal.inject.dependency.InjectionNode;
import packed.internal.service.runtime.DelegatingRuntimeService;
import packed.internal.service.runtime.RuntimeService;
import packed.internal.service.runtime.ServiceInstantiationContext;

/** An entry specifically used for {@link ServiceExtension#provideAll(ServiceLocator)}. */
public final class RuntimeAdaptorServiceSetup extends ServiceSetup {

    /** The runtime entry to delegate to. */
    private final RuntimeService adapt;

    public RuntimeAdaptorServiceSetup(RuntimeService adapt) {
        super(adapt.key());
        this.adapt = adapt;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public InjectionNode dependant() {
        return null; // runtime entries never has any unresolved dependencies
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return adapt.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return adapt.isConstant();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        return new DelegatingRuntimeService(key(), adapt);
    }
}
