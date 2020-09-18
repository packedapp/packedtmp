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

    public MethodSidecarModel msm;

    public MethodHandle directMethodHandle;

    public List<DependencyDescriptor> dependencies;

    public static void main(Method method) {
        MethodDescriptor m = MethodDescriptor.from(method);
        List<DependencyDescriptor> fromExecutable = DependencyDescriptor.fromExecutable(m);
    }

    @Nullable
    RunAt runAt;

    enum RunAt {
        INITIALIZATION;
    }
}
