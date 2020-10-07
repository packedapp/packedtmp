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
package packed.internal.component;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.component.SourceModelMethod.RunAt;
import packed.internal.inject.dependency.DependencyDescriptor;

/**
 *
 */
// SourceModel...
// Maa have en liste af regions slots den skal bruge
public abstract class SourceModelMember {
    public Key<?> provideAskey;

    public boolean provideAsConstant;

    public List<DependencyDescriptor> dependencies;

    @Nullable
    public RunAt runAt = RunAt.INITIALIZATION;
    // Jeg tror man loeber alle parameterene igennem og ser om der
    // er en sidecar provide der passer dem
    // Saa man sidecar providen dertil.

    // Sidecar provideren tager i oevrigt RegionAssembly
    public abstract int getModifiers();

    public abstract MethodHandle methodHandle();
}
