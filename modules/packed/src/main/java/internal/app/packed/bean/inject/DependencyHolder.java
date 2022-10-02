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
package internal.app.packed.bean.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import internal.app.packed.operation.bindings.DependencyProducer;
import internal.app.packed.operation.bindings.InternalDependency;

/**
 *
 */
abstract class DependencyHolder {

    /** Dependencies that needs to be resolved. */
    final List<InternalDependency> dependencies;

    final boolean provideAsConstant;

    // Jeg tror man loeber alle parameterene igennem og ser om der
    // er en sidecar provide der passer dem
    // Saa man sidecar providen dertil.
    @Nullable
    public final Key<?> provideAskey;

    DependencyHolder(List<InternalDependency> dependencies, boolean provideAsConstant, Key<?> provideAsKey) {
        this.provideAskey = provideAsKey;
        this.dependencies = requireNonNull(dependencies);
        this.provideAsConstant = provideAsConstant;
    }

    public abstract DependencyProducer[] createProviders();
    
    public abstract boolean isStatic();
    
    public abstract MethodHandle methodHandle();

}
