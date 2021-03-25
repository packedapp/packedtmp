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
package packed.internal.inject.service.runtime;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Provider;
import app.packed.inject.ProvisionContext;
import app.packed.inject.ServiceLocator;
import app.packed.inject.ServiceMode;
import packed.internal.inject.PackedProvisionContext;
import packed.internal.inject.service.PackedService;

/** An entry that represents a service at runtime. */
public abstract class RuntimeService implements PackedService {

    @Override
    public <T> PackedService decorate(Function<? super T, ? extends T> decoratingFunction) {
        throw new UnsupportedOperationException();
    }

    // We need this to adapt to build time transformations
    public abstract MethodHandle dependencyAccessor();

    /**
     * Returns an instance.
     * 
     * @param request
     *            a request if needed by {@link #requiresProvisionContext()}
     * @return the instance
     */
    public abstract Object getInstance(@Nullable ProvisionContext request);

    final Object getInstanceForLocator(ServiceLocator locator) {
        ProvisionContext pc = PackedProvisionContext.of(key());
        Object t = getInstance(pc);
        return t;
    }

    final Provider<?> getProviderForLocator(ServiceLocator locator) {
        if (isConstant()) {
            Object t = getInstanceForLocator(locator);
            return Provider.ofConstant(t);
        } else {
            ProvisionContext pc = PackedProvisionContext.of(key());
            return new ServiceWrapperProvider<Object>(this, pc);
        }
    }

    @Override
    public final boolean isConstant() {
        return mode() == ServiceMode.CONSTANT;
    }


    @Override
    public final PackedService rekeyAs(Key<?> key) {
        return new DelegatingRuntimeService(this, key);
    }

    public abstract boolean requiresProvisionContext();

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key());
        sb.append("[mode=").append(mode()).append(']');
        return sb.toString();
    }
}
