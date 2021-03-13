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
package packed.internal.inject.service.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.inject.Dependant;
import packed.internal.inject.service.runtime.ConstantRuntimeService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/** A build-time service for a constant. */
public final class ConstantBuildtimeService extends BuildtimeService {

    /** The constant we are wrapping. */
    private final Object constant;

    /**
     * @param key
     */
    public ConstantBuildtimeService(Key<?> key, Object constant) {
        super(key);
        this.constant = requireNonNull(constant, "constant is null");
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable Dependant dependant() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return MethodHandles.constant(key().rawType(), constant);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        return new ConstantRuntimeService(key(), constant);
    }
}
