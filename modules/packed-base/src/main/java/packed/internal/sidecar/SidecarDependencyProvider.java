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
package packed.internal.sidecar;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Provide;
import packed.internal.component.RuntimeRegion;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.inject.dependency.Injectable;

/**
 * Represents a method on a sidecar annotated with {@link Provide}.
 */
public final class SidecarDependencyProvider implements DependencyProvider {

    /** The key under which the dependency is provided. */
    public final Key<?> key;

    private final MethodHandle methodHandle;

    /** The sidecar that provides the instance. */
    public final SidecarModel<?> sidecarModel;

    private SidecarDependencyProvider(SidecarModel<?> sidecarModel, Builder builder) {
        this.key = builder.key;
        this.sidecarModel = sidecarModel;
        MethodHandle mh = builder.methodHandle;

        this.methodHandle = MethodHandles.dropArguments(mh, 0, RuntimeRegion.class);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return methodHandle;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Injectable getInjectable() {
        return null;
    }

    public static class Builder {

        public final Key<?> key;

        private final MethodHandle methodHandle;

        Builder(Method method, MethodHandle methodHandle) {
            this.key = Key.fromMethodReturnType(method);
            this.methodHandle = requireNonNull(methodHandle);
        }

        SidecarDependencyProvider build(SidecarModel<?> sidecarModel) {
            return new SidecarDependencyProvider(sidecarModel, this);
        }
    }
}
