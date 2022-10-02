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
package internal.app.packed.service.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class DependencyHolder {

    /** Dependencies that needs to be resolved. */
    final List<InternalDependency> dependencies;

    final boolean isStatic;

    final MethodHandle mh;

    final boolean provideAsConstant;

    @Nullable
    public final Key<?> provideAskey;

    public DependencyHolder(List<InternalDependency> dependencies, boolean provideAsConstant, Key<?> provideAsKey, boolean isStatic, MethodHandle mh) {
        this.provideAskey = provideAsKey;
        this.dependencies = requireNonNull(dependencies);
        this.provideAsConstant = provideAsConstant;
        this.isStatic = isStatic;
        this.mh = mh;
    }

    public DependencyHolder(boolean provideAsConstant, Key<?> provideAsKey, OperationSetup os) {
        this.dependencies = InternalDependency.fromOperationType(os.type);

        this.provideAskey = provideAsKey;
        this.provideAsConstant = provideAsConstant;
        this.isStatic = os.target.isStatic;
        this.mh = os.target.methodHandle;
    }

    public final DependencyProducer[] createProviders() {
        DependencyProducer[] providers = new DependencyProducer[isStatic() ? 0 : 1];
        return providers;
    }

    final boolean isStatic() {
        return isStatic;
    }

    final MethodHandle methodHandle() {
        return mh;
    }
}
