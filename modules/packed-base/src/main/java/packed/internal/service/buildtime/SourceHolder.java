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
package packed.internal.service.buildtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.inject.ProvidePrototypeContext;
import packed.internal.component.Region;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.buildtime.service.ComponentBuildEntry;
import packed.internal.service.buildtime.service.ComponentConstantBuildEntry;
import packed.internal.service.buildtime.service.ComponentMethodHandleBuildEntry;
import packed.internal.service.buildtime.service.ServiceProvidingManager;
import packed.internal.util.KeyBuilder;

/**
 *
 */
public class SourceHolder {

    /** Just an empty reusable array. */
    private static final BuildEntry<?>[] NO_DEP = new BuildEntry<?>[0];

    /** The dependencies of this node. */
    public final List<ServiceDependency> dependencies;

    /** Whether or this node contains a dependency on {@link ProvidePrototypeContext}. */
    public final boolean hasDependencyOnProvidePrototypeContext;

    /** The resolved dependencies of this node. */
    public final BuildEntry<?>[] resolvedDependencies;

    public final int offset;

    public final MethodHandle mha;

    public final int index;

    /** The instantiation mode of this node. */
    final ServiceMode instantionMode;

    public MethodHandle reducedMha;

    public SourceHolder(List<ServiceDependency> dependencies, @Nullable ComponentBuildEntry<?> declaringEntry, MethodHandle mh, ServiceMode sm, int index) {
        this.dependencies = requireNonNull(dependencies);
        this.mha = requireNonNull(mh);
        this.index = index;
        this.instantionMode = sm;
        int depSize = dependencies.size();

        // We include the declaring entry in resolved dependencies because we want to use it when
        // checking for dependency circles...
        if (declaringEntry == null) {
            this.offset = 0;
            this.resolvedDependencies = depSize == 0 ? NO_DEP : new BuildEntry<?>[depSize];
        } else {
            this.offset = 1;
            this.resolvedDependencies = new BuildEntry<?>[depSize + 1];
            this.resolvedDependencies[0] = declaringEntry;
        }

        boolean hasDependencyOnProvidePrototypeContext = false;
        if (!dependencies.isEmpty()) {
            for (ServiceDependency e : dependencies) {
                if (e.key().equals(KeyBuilder.INJECTION_SITE_KEY)) {
                    hasDependencyOnProvidePrototypeContext = true;
                    break;
                }
            }
        }
        this.hasDependencyOnProvidePrototypeContext = hasDependencyOnProvidePrototypeContext;
    }

    public MethodHandle newMH(ServiceProvidingManager context) {
        MethodHandle mh = mha;

        boolean resolveDeclaringEntry = dependencies.size() != mh.type().parameterCount();
        if (resolveDeclaringEntry) {
            MethodHandle mhp = resolvedDependencies[0].toMH(context);
            mh = MethodHandles.collectArguments(mh, 0, mhp);
            context.mustInstantiate.addLast(this);
            reducedMha = mh;
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
                MethodHandle collect = c.toMH(context);
                mh = MethodHandles.collectArguments(mh, index, collect);
            } else {}
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
        // System.out.println("*********************** MUST INSTANTIATE " + component);
        context.mustInstantiate.addLast(this);
        if (instantionMode == ServiceMode.CONSTANT) {

            // TODO It must be read -> it must be written...

            return Region.readSingletonAs(index, mh.type().returnType());
        } else {
            return mh;
        }
    }
}
