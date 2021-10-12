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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Key;

/** An runtime service holding a constant. */
record ConstantRuntimeService(Key<?> key, Object constant) implements RuntimeService {

    ConstantRuntimeService {
        requireNonNull(key);
        requireNonNull(constant);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return MethodHandles.constant(key().rawType(), constant);
    }

    /** {@inheritDoc} */
    @Override
    public Object provideInstance() {
        return constant;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresProvisionContext() {
        return false;
    }

    @Override
    public String toString() {
        return RuntimeService.toString(this);
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
