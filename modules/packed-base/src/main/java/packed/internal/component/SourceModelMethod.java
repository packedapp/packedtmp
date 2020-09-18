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

/**
 *
 */
// run on initialize
// run on start
// run on stop

// En per annotering

// Altsaa alle source metoder skal jo resolves paa assembly time

public class SourceModelMethod extends SourceModelMember {

    // Dependencies that have already been resolved

    public DependencyProvider[] resolved;

    public final MethodHandle directMethodHandle;

    public final List<DependencyDescriptor> dependencies;

    public final MethodSidecarModel model;

    public final Method method;

    SourceModelMethod(Method method, MethodSidecarModel model, MethodHandle mh) {
        this.method = requireNonNull(method);
        this.model = requireNonNull(model);
        MethodDescriptor m = MethodDescriptor.from(method);
        this.dependencies = DependencyDescriptor.fromExecutable(m);
        this.directMethodHandle = requireNonNull(mh);
    }

    @Nullable
    RunAt runAt = RunAt.INITIALIZATION;

    enum RunAt {
        INITIALIZATION;
    }
}
