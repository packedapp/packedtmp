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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.introspection.MethodDescriptor;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarDependencyProvider;

/**
 *
 */
// run on initialize
// run on start
// run on stop

// En per annotering

// Altsaa alle source metoder skal jo resolves paa assembly time

public class SourceModelMethod extends SourceModelMember {

    public final List<DependencyDescriptor> dependencies;

    /** A direct method handle to the method. */
    public final MethodHandle directMethodHandle;

    public final Method method;

    public final MethodSidecarModel model;

    @Nullable
    RunAt runAt = RunAt.INITIALIZATION;

    SourceModelMethod(Method method, MethodSidecarModel model, MethodHandle mh) {
        this.method = requireNonNull(method);
        this.model = requireNonNull(model);
        MethodDescriptor m = MethodDescriptor.from(method);
        this.dependencies = DependencyDescriptor.fromExecutable(m);
        this.directMethodHandle = requireNonNull(mh);
    }

    enum RunAt {
        INITIALIZATION;
    }

    public void resolved() {

    }

    public DependencyProvider[] createProviders() {
        DependencyProvider[] providers = new DependencyProvider[directMethodHandle.type().parameterCount()];
        System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor d = dependencies.get(i);
            SidecarDependencyProvider dp = model.keys.get(d.key());
            if (dp != null) {
                System.out.println("MAtches for " + d.key());
                int index = i + directMethodHandle.type().parameterCount() == dependencies.size() ? 0 : 1;
                providers[index] = dp;
                System.out.println("SEtting provider " + dp.dependencyAccessor());
            }
        }

        return providers;
    }
}
