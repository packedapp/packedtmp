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
package packed.internal.hooks;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Provide;
import packed.internal.component.RuntimeRegion;
import packed.internal.inject.Dependant;
import packed.internal.inject.DependencyProvider;

/**
 * Represents a method on a sidecar annotated with {@link Provide}.
 */
public final class ContextMethodProvide implements DependencyProvider {

    /** The key under which the dependency is provided. */
    public final Key<?> key;

    /** A (bound if needed) method handle. */
    private final MethodHandle methodHandle;

    /** The sidecar that provides the instance. */
    public final AbstractHookBootstrapModel<?> sidecarModel;

    private ContextMethodProvide(AbstractHookBootstrapModel<?> sidecarModel, Builder builder) {
        this.key = builder.key;
        this.sidecarModel = sidecarModel;
        this.methodHandle = MethodHandles.dropArguments(builder.methodHandle, 0, RuntimeRegion.class);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return methodHandle;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Dependant dependant() {
        return null;
    }

    static class Builder {

        final Key<?> key;

        final MethodHandle methodHandle;

        Builder(Method method, MethodHandle methodHandle) {
            this.key = Key.convertMethodReturnType(method);
            this.methodHandle = requireNonNull(methodHandle);
        }

        ContextMethodProvide build(AbstractHookBootstrapModel<?> sidecarModel) {
            return new ContextMethodProvide(sidecarModel, this);
        }
    }
}
