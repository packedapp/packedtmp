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

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.inject.dependency.Injectable;

/**
 *
 */
public final class SidecarDependencyProvider implements DependencyProvider {

    public final Key<?> key;

    private final MethodHandle methodHandle;

    final SidecarModel<?> sidecarModel;

    private SidecarDependencyProvider(SidecarModel<?> sidecarModel, Builder builder) {
        this.key = builder.key;
        this.sidecarModel = sidecarModel;
        this.methodHandle = builder.methodHandle;
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

        private final Key<?> key;

        private final MethodHandle methodHandle;

        Builder(Key<?> key, MethodHandle methodHandle) {
            this.key = requireNonNull(key);
            this.methodHandle = requireNonNull(methodHandle);
        }

        SidecarDependencyProvider build(SidecarModel<?> sidecarModel) {
            return new SidecarDependencyProvider(sidecarModel, this);
        }
    }
}
