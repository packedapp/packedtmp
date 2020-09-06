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

import java.util.List;

import app.packed.base.Nullable;
import app.packed.inject.ProvidePrototypeContext;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.buildtime.service.AbstractComponentBuildEntry;
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

    public SourceHolder(List<ServiceDependency> dependencies, @Nullable AbstractComponentBuildEntry<?> declaringEntry) {
        this.dependencies = requireNonNull(dependencies);
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

    public final void checkResolved() {
        for (int i = 0; i < resolvedDependencies.length; i++) {
            BuildEntry<?> n = resolvedDependencies[i];
            if (n == null && !dependencies.get(i).isOptional()) {
                throw new AssertionError("Dependency " + dependencies.get(i) + " was not resolved");
            }
        }
    }
}
