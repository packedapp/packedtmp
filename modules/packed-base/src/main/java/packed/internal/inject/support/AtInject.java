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
package packed.internal.inject.support;

import static java.util.Objects.requireNonNull;

import java.util.List;

import packed.internal.inject.InternalDependency;
import packed.internal.invokers.AccessibleMember;

/**
 *
 */
public final class AtInject extends AbstractAccessibleMember {

    public final List<InternalDependency> dependencies;

    /**
     * @param am
     */
    AtInject(AccessibleMember<?> am, List<InternalDependency> dependencies) {
        super(am);
        this.dependencies = requireNonNull(dependencies);
    }

    AtInject(AccessibleMember<?> am, InternalDependency dependency) {
        super(am);
        this.dependencies = List.of(dependency);
    }
}
