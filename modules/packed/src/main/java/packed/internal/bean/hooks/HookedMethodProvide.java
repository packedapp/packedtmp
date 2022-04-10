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
package packed.internal.bean.hooks;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.hooks.scrap.ScopedProvide;
import packed.internal.inject.DependencyNode;
import packed.internal.inject.DependencyProducer;
import packed.internal.lifetime.LifetimePool;

/**
 * Represents a method on a hook class annotated with {@link ScopedProvide}.
 */
public final class HookedMethodProvide implements DependencyProducer {

    /** The key under which the dependency is provided. */
    public final Key<?> key;

    /** A (bound if needed) method handle. */
    private final MethodHandle methodHandle;

    /** The sidecar that provides the instance. */
    public final AbstractHookModel<?> hookModel;

    private HookedMethodProvide(AbstractHookModel<?> sidecarModel, Builder builder) {
        this.key = builder.key;
        this.hookModel = sidecarModel;
        this.methodHandle = MethodHandles.dropArguments(builder.methodHandle, 0, LifetimePool.class);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return methodHandle;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DependencyNode dependencyConsumer() {
        return null;
    }

    static class Builder {

        final Key<?> key;

        final MethodHandle methodHandle;

        Builder(Method method, MethodHandle methodHandle) {
            this.key = Key.convertMethodReturnType(method);
            this.methodHandle = requireNonNull(methodHandle);
        }

        HookedMethodProvide build(AbstractHookModel<?> sidecarModel) {
            return new HookedMethodProvide(sidecarModel, this);
        }
    }
}
