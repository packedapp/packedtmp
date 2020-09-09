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
package packed.internal.inject.resolvable;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import app.packed.base.Nullable;
import packed.internal.component.Region;
import packed.internal.component.RegionAssembly;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.buildtime.service.ComponentBuildEntry;
import packed.internal.service.buildtime.service.ComponentConstantBuildEntry;
import packed.internal.service.buildtime.service.ComponentMethodHandleBuildEntry;
import packed.internal.service.buildtime.service.ServiceProvidingManager;

/**
 *
 */
public class ResolvableFactory {

    /** The dependencies of this node. */
    public final List<ServiceDependency> dependencies;

    /** The resolved dependencies of this node. */
    public final BuildEntry<?>[] resolvedDependencies;

    public final int dependencyOffset;

    private final MethodHandle mha;

    public final int regionIndex;

    /** The instantiation mode of this node. */
    private final ServiceMode instantionMode;

    public MethodHandle reducedMha;

    public BuildEntry<?> buildEntry;

    public ResolvableFactory(List<ServiceDependency> dependencies, @Nullable ComponentBuildEntry<?> declaringEntry, MethodHandle mh, ServiceMode sm,
            int index) {
        this.dependencies = requireNonNull(dependencies);
        this.mha = requireNonNull(mh);
        this.regionIndex = index;
        this.instantionMode = sm;
        int depSize = dependencies.size();

        // We include the declaring entry in resolved dependencies because we want to use it when
        // checking for dependency circles...
        if (declaringEntry == null) {
            this.dependencyOffset = 0;
            this.resolvedDependencies = new BuildEntry<?>[depSize];
        } else {
            this.dependencyOffset = 1;
            this.resolvedDependencies = new BuildEntry<?>[depSize + 1];
            this.resolvedDependencies[0] = declaringEntry;
        }
    }

    public MethodHandle newMH(RegionAssembly ra, ServiceProvidingManager context) {
        MethodHandle mh = mha;

        boolean resolveDeclaringEntry = dependencies.size() != mh.type().parameterCount();
        if (resolveDeclaringEntry) {
            MethodHandle mhp = resolvedDependencies[0].toMH(ra, context);
            mh = MethodHandles.collectArguments(mh, 0, mhp);
            reducedMha = mh;
            ra.mustInstantiate.addLast(this);
            return mh;
        }

        int adjust = 0;
        for (int i = 0; i < resolvedDependencies.length; i++) {
            int index = i == 0 ? 0 : i - adjust;
            BuildEntry<?> e = resolvedDependencies[i];
            requireNonNull(e);
            if (e instanceof ComponentConstantBuildEntry) {
                ComponentConstantBuildEntry<?> c = (ComponentConstantBuildEntry<?>) e;
                Object instance = c.component.source.instance();
                mh = MethodHandles.insertArguments(mh, index, instance); // 0 is NodeStore
                adjust++;
            } else if (e instanceof ComponentMethodHandleBuildEntry) {
                ComponentMethodHandleBuildEntry<?> c = (ComponentMethodHandleBuildEntry<?>) e;
                MethodHandle collect = c.toMH(ra, context);
                mh = MethodHandles.collectArguments(mh, index, collect);
            }
        }
        if (mh.type().parameterCount() == 0) {
            mh = MethodHandles.dropArguments(mh, 0, Region.class);
        } else if (mh.type().parameterCount() > 1) {
            MethodType mt = MethodType.methodType(mh.type().returnType(), Region.class);
            int[] ar = new int[mh.type().parameterCount()];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = 0;
            }
            mh = MethodHandles.permuteArguments(mh, mt, ar);
        }
        reducedMha = mh;
        if (instantionMode == ServiceMode.CONSTANT) {
            ra.mustInstantiate.addLast(this);
            return Region.readSingletonAs(regionIndex, mh.type().returnType());
        } else {
            return mh;
        }
    }
}
