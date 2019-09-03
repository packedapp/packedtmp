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
package packed.internal.inject.util;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.List;

import app.packed.inject.Provide;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;

/** A descriptor for a member annotated with {@link Provide}. */
public final class AtInject {

    /** The annotated member, either an {@link FieldDescriptor} or an {@link MethodDescriptor}. */
    public final Member member;

    /** The dependencies (parameters) of the member. */
    public final List<PackedServiceDependency> dependencies;

    /** A unbound method handle to the underlying field or method. */
    public final MethodHandle methodHandle;

    public AtInject(MethodHandle mh, Member member, List<PackedServiceDependency> dependencies) {
        this.methodHandle = requireNonNull(mh);
        this.dependencies = requireNonNull(dependencies);
        this.member = requireNonNull(member);
    }
}
