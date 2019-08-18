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
package packed.internal.inject.annotations;

import static java.util.Objects.requireNonNull;

import java.util.List;

import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.invoke.InvokableMember;

/** A descriptor of an annotated member that has 0 or more service dependencies. */
public class AtDependable {

    /** The dependencies (parameters) of the member. */
    public final List<InternalDependencyDescriptor> dependencies;

    /** The invokable member. */
    public final InvokableMember<?> invokable;

    /**
     * Creates a new AtDependable.
     * 
     * @param invokable
     *            the invokable member
     * @param dependencies
     *            a list of dependencies
     */
    AtDependable(InvokableMember<?> invokable, List<InternalDependencyDescriptor> dependencies) {
        this.invokable = requireNonNull(invokable);
        this.dependencies = requireNonNull(dependencies);
    }
}
